package groovyx.sql.dsl.expr
import org.codehaus.groovy.syntax.*

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.ast.expr.ExpressionStatement
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

//import static groovyx.sql.dsl.SqlQueryASTTransformation.*
/**
 * 
 * @author V. Shyshkin
 */
class DslQueryVisitor extends ClassCodeVisitorSupport {
    
    def state
    def sourceUnit
    
    def rootExpression
    
    def methods = []
    def supportedMethods = ["from","join","leftJoin","rightJoin","fullJoin"]
    
/*    def DslQueryVisitor(sourceUnit,state) {
        this.sourceUnit = sourceUnit
        this.state = state
    }
*/    
    public void visitClosureExpression(ClosureExpression expression) {
        expression.getCode().visit(this);
        //println "CLOSURE: " + expression.code.class
    }
    public void visitBlockStatement(BlockStatement block) {
/*        for (Statement statement : block.getStatements()) {
            statement.visit(this);
        }
*/        
        def msg = "Unsupported expression. Must be method call."
        block.statements.each() {
            if ( ! (it instanceof ExpressionStatement && it.expression instanceof MethodCallExpression) ) {
                addError(msg,it)
                return
            }
            //methods << it.expression.method.text
            
            def object = it.expression
            def cond = true
            while (cond) {
                if ( object instanceof MethodCallExpression ) {
                    methods << object.method.text
                    object = object.objectExpression
                } else {
                    addError(msg,object)
                    return
                }
                cond = ! (object instanceof VariableExpression && "this" == object.text) 
            }//while
        }//each
        methods.each {
            println it
        }
        
        methods = methods.reverse()
   /*     methods.each {
            if ( ! (object.method.text.toLowerCase() in supportedMethods) )  {
                addError("Unsupported method name '${object.method.text}'.",object.method)
                return
            }
            
        }
*/        

        

    }
    
    def addError(msg,expr) {
        groovyx.sql.dsl.SqlQueryASTTransformation.addError(sourceUnit,msg,expr)
    }
            
/*    void visitMethodCallExpression(MethodCallExpression expr) {
        //println "visitMethodCallExpression.method="  + expr.method.text  

        expr.objectExpression.visit(this)
        //def obj = whereList[whereList.size()-1]
        def obj = whereList.pop()
        expr.arguments.visit(this)
                
        //def args = whereList[whereList.size()-1]
        def args = whereList.pop()
                
        def newExpr = new ConstructorCallExpression(
            ClassHelper.make(SqlMethodCallExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('object'), obj),                        
                        new MapEntryExpression(new ConstantExpression('method'), new ConstantExpression(expr.method.text)),                        
                        new MapEntryExpression(new ConstantExpression('arguments'), args)                        
                
                    ]) ) )
        whereList << newExpr
        rootExpression = newExpr

    }    
  */          
            
    protected SourceUnit getSourceUnit() { sourceUnit }
    
}

