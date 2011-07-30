package groovyx.sql.dsl.expr
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
class WhereValidateVisitor extends ClassCodeVisitorSupport {
    
    def rootExpression
    def errors = []
    def ownerCall
    def owner
    
    def whereList = []
    
    void visitBinaryExpression(BinaryExpression expr) {
        if ( ! isOperationSupported(expr.operation.text)) {
            errors << ["Unsupported binary operation '${expr.operation.text}'",expr]
            return
        }
        expr.leftExpression.visit(this)
        if ( errorFound()) {
            return
        }
//        def l = whereList.pop()

        expr.rightExpression.visit(this)
        if ( errorFound()) {
            return
        }
        
/*        def r = whereList.pop()
        def newExpr = new ConstructorCallExpression(
            ClassHelper.make(SqlBinaryExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('leftExpression'), l),                        
                        new MapEntryExpression(new ConstantExpression('operation'), new ConstantExpression(expr.operation.text)),                        
                        new MapEntryExpression(new ConstantExpression('rightExpression'), r)                        
                
                    ]) ) )
                
        whereList << newExpr
        rootExpression = newExpr
  */              
    }
    protected boolean isOperationSupported(String op) {
        return ["+","-","*","/","**","==","!=","<","<=",">",">=","&&","||"].contains(op)
    }
    protected boolean errorFound() {
        ! errors.isEmpty()
    }
    
    void startVisit() {
        if ( ownerCall.arguments.expressions.isEmpty() ) {
            errors << ["Method '${ownerCall.method.text}' must have exactly one argument",call]
            return
        }
        def arg = ownerCall.arguments.expressions[0]
        if ( ! ( 
                arg instanceof BinaryExpression  ||
                arg instanceof MethodCallExpression
               ) 
           )
        {
            errors << ["Unsupported expression '${arg.text}' (method '${ownerCall.method.text}')",arg]
            return
        }
        ownerCall.arguments.visit(this)
        if ( errorFound() ) {
            return
        }

    }
    void visitConstantExpression(ConstantExpression expr) {
        if ( ! validateConstant(expr) ) {
            errors << ["Invalid constant '${expr.value}'",expr]
        }
        /*        whereList << new ConstructorCallExpression(
        ClassHelper.make(SqlConstantExpression),
        new TupleExpression(new NamedArgumentListExpression([
        new MapEntryExpression(new ConstantExpression('value'),expr)
        ])))
         */                    
    }
    protected boolean validateConstant(expr) {
        boolean result = true
        if ( expr.value instanceof Float ) {
            result = false
        }
        return result
    }
    void visitVariableExpression(VariableExpression expr) {
        if ( ! SqlVars.contains(expr.name) ) {
            errors << ["Unsupported variable  '${expr.name}'",expr]
            return
        }

/*        whereList << new ConstructorCallExpression(
            ClassHelper.make(SqlVariableExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('name'),new ConstantExpression(expr.name))
                    ])))
*/                    
    }
            
    void visitMethodCallExpression(MethodCallExpression expr) {
        //println "visitMethodCallExpression.method="  + expr.method.text  

        expr.objectExpression.visit(this)

/*        def obj = whereList.pop()
        expr.arguments.visit(this)
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
*/
    }    
            
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        println "------ visitArgumentlistExpression"
        visitTupleExpression(ale);
        if ( errorFound() ) {
            return
        }
        /*        def newExpr = new ConstructorCallExpression(
            
        ClassHelper.make(SqlArgumentListExpression),
        new TupleExpression(new NamedArgumentListExpression([
        new MapEntryExpression(new ConstantExpression('arguments'), 
        new ListExpression(whereList[whereList.size()-1]) )
                
        ]) ) )
        whereList << newExpr
        rootExpression = newExpr
         */                
    }
            
    public void visitNotExpression(NotExpression expression) {
        expression.getExpression().visit(this);
        def newExpr = new ConstructorCallExpression(
            
            ClassHelper.make(SqlNotExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('expression'), 
                            whereList[whereList.size()-1] )
                
                    ]) ) )
        whereList << newExpr
        rootExpression = newExpr
                
    }

    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }
    void visitListOfExpressions(List<? extends Expression> list) {
        if (list == null) {
        }
        def l = []
        def i = 0
        for (Expression expression : list) {
            if (expression instanceof SpreadExpression) {
                Expression spread = ((SpreadExpression) expression).getExpression();
                spread.visit(this);
            } else {
                expression.visit(this);
            }
            if ( errorFound() ) {
                return
            }

            /*            l << new ConstructorCallExpression(
            
            ClassHelper.make(SqlArgumentExpression),
            new TupleExpression(new NamedArgumentListExpression([
            new MapEntryExpression(new ConstantExpression('argument'), whereList[whereList.size()-1])//,                        
            ])) )
             */                    
        }//for
        //        whereList << l
    }
    public void visitPropertyExpression(PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
        def obj = whereList[whereList.size()-1]
        expression.getProperty().visit(this);
        def prop = whereList[whereList.size()-1]
        
        def newExpr = new ConstructorCallExpression(
            ClassHelper.make(SqlPropertyExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('object'), obj),
                        new MapEntryExpression(new ConstantExpression('property'), prop)//,                                            

                    ])) )
        whereList << newExpr
        rootExpression = newExpr

    }
    public void visitCastExpression(CastExpression expr) {
        expr.getExpression().visit(this);
        
        def newExpr = new ConstructorCallExpression(
            ClassHelper.make(SqlCastExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('type'),new ConstantExpression(expr.type.name)),
                        new MapEntryExpression(new ConstantExpression('expression'),whereList[whereList.size()-1])
                    ])) )
        whereList << newExpr
        rootExpression = newExpr
        
    }
            
    protected SourceUnit getSourceUnit() { source }
    
}

