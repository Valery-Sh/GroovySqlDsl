
/*
 * 111
 */
package groovyx.sql.dsl

import org.codehaus.groovy.syntax.*

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.ClassExpression

import org.codehaus.groovy.ast.expr.CastExpression

import groovyx.sql.dsl.expr.builder.*
import groovyx.sql.dsl.predef.*
import groovyx.sql.dsl.expr.*
/**
 *
 * @author V. Shyshkin
 */
//@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class SqlQueryASTTransformation implements ASTTransformation {

    void visit(ASTNode[] nodes, SourceUnit source) {
        def logicalExpressionVisitor = new LogicalExpressionVisitor()        
        
        def whereMethodVisitor = new ClassCodeVisitorSupport() {
            
            def whereExpr
            def whereMethodCall
            
            void visitBinaryExpression(BinaryExpression binExpr) {
                super.visitBinaryExpression(binExpr)
            }
                        
            void visitMethodCallExpression(MethodCallExpression clauseCall) {
                // transform "where a op b" into "where WhereClause(a, op, b)"
                if (
                    clauseCall.method instanceof ConstantExpression &&
                    (clauseCall.method.value == "where" || clauseCall.method.value == "join") &&
                    clauseCall.arguments instanceof ArgumentListExpression &&
                    clauseCall.arguments.expressions.size() == 1 &&
                    ( clauseCall.arguments.expressions[0] instanceof BinaryExpression ||
                        clauseCall.arguments.expressions[0] instanceof NotExpression )  
                ) {
                    whereMethodCall = clauseCall
                    clauseCall.arguments.expressions[0].visit(logicalExpressionVisitor)
                    whereExpr = logicalExpressionVisitor.rootExpression
                }                    
            }

            void visitClosureExpression(ClosureExpression expression) {
                super.visitClosureExpression(expression)
            }

            protected SourceUnit getSourceUnit() { 
                source 
            }
        }

        def queryMethodVisitor = new ClassCodeVisitorSupport() {
            def whereExpr
            def whereMethodCall
            void visitMethodCallExpression(MethodCallExpression call) {
                def exprTemp = call.arguments.expressions[0].getClass()
                println "exprTemp: " + exprTemp
                if (
                    // 'sqlAdapter' variable
                    call.objectExpression instanceof VariableExpression && call.objectExpression.variable == 'sqlAdapter' &&
                    // 'query' or 'execute' method
                    call.method instanceof ConstantExpression && call.method.value == 'query' &&
                    // closure single argument
                    call.arguments.expressions.size() == 1 && call.arguments.expressions[0] instanceof ClosureExpression
                ) {
                    ClosureExpression closureExpr = call.arguments.expressions[0]
                    whereMethodVisitor.visitClosureExpression(closureExpr)
                    whereExpr = whereMethodVisitor.whereExpr
                    whereMethodCall = whereMethodVisitor.whereMethodCall
                } else {
                    super.visitMethodCallExpression(call)
                }
            }

            protected SourceUnit getSourceUnit() { source }
        }
        
        source.AST.classes.each { ClassNode cn ->
            queryMethodVisitor.visitClass(cn)
            queryMethodVisitor.whereMethodCall.arguments = queryMethodVisitor.whereExpr
        }
    }//visit
    
    /////////////////////////////////

    def getTransformation(expr,saveOperation=null) {
        def result
        if ( expr instanceof NotExpression ) {
            result  =  createMethodCall(expr.expression,SqlNotExpression,saveOperation)
        } else if (expr instanceof VariableExpression && SqlVars.contains(expr.name) ) {
            result  =  createVariableMethodCall(expr,expr.name,saveOperation)            
        } else if (expr instanceof MethodCallExpression){
            //            result  =  createMethodCall(expr,SqlFunctionCallExpression,saveOperation)            
            result  =  createFunctionMethodCall(expr,saveOperation)            
            //result = null
        } 
        
        else if (expr instanceof ConstantExpression){
            result  =  createScalarMethodCall(expr,expr.value,saveOperation)            
        } else if (expr instanceof ArgumentListExpression){
            result  =  createArgumentListMethodCall(expr)            
        }  else {
            result = createMethodCall(expr,SqlExpression,saveOperation)
        }
        return result
    }
    
    
}//class