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
    def ownerCall
    def owner
    
    def whereList = []
    
    void visitBinaryExpression(BinaryExpression expr) {
        if ( ! isOperationSupported(expr.operation.text)) {
            addError("Unsupported binary operation '${expr.operation.text}'",expr)
            return
        }
        
        if ( ! isSupportedExpression(expr.leftExpression) ) {
            addError("Unsupported binary expression '${expr.leftExpression.text}' (method '${ownerCall.method.text}', left part)",expr.leftExpression)            
            return
        }
        
        expr.leftExpression.visit(this)
        if ( errorFound()) {
            return
        }

        if ( ! isSupportedExpression(expr.rightExpression) ) {
            addError("Unsupported binary  expression '${expr.leftExpression.text}' (method '${ownerCall.method.text}', , right part)",expr.rightExpression)
            return
        }
        expr.rightExpression.visit(this)
    }
    protected boolean isOperationSupported(String op) {
        return ["+","-","*","/","**","==","!=","<","<=",">",">=","&&","||"].contains(op)
    }
    protected boolean errorFound() {
        return owner.sourceUnit.getErrorCollector().hasErrors()
    }
    /**
     * <code>Where</code> or <code>On</code> must have exactly one argument.
     * The argument expression may by one of the follows:
     * <ul>
     *    <li><code>ConstantExpression</code>. The value must be of type 
     *    <code>java.lang.Boolean</code></li>  
     *    <li><code>MethodCallExpression</code>. We need investigate is there
     *     a function that returns <code>java.lang.Boolean</code> value</li>  
     *    <li><code>PropertyExpression</code>. The property must be of 
     *     <code>java.lang.Boolean</code> type</li>  
     *    <li><code>BinaryExpression</code>. The operation must be logical or 
     *    comparison operation.</li>
     *    <li><code>NotExpression</code>.</li>
     * </ul>
     */
    void startVisit() {
        if ( ownerCall.arguments.expressions.isEmpty() ) {
            //errors << ["Method '${ownerCall.method.text}' must have exactly one argument",call]
            addError("Method '${ownerCall.method.text}' must have exactly one argument",call)            
            return
        }
        def arg = ownerCall.arguments.expressions[0]
        if ( ! ( 
                arg instanceof BinaryExpression  ||
                arg instanceof NotExpression  ||
                arg instanceof MethodCallExpression ||
                arg instanceof PropertyExpression ||
                arg instanceof ConstantExpression && arg.value instanceof Boolean  
               ) 
           )
        {
            //errors << ["Unsupported expression '${arg.text}' (method '${ownerCall.method.text}')",arg]
            addError("Unsupported expression '${arg.text}' (method '${ownerCall.method.text}')",arg)            
            return
        }
        ownerCall.arguments.expressions[0].visit(this) //visit the expression not argumentList
        //ownerCall.arguments.visit(this)
        if ( errorFound() ) {
            return
        }

    }
    
    void addError(msg,expr) {
        owner.addError(msg,expr)
    }
    
    void visitConstantExpression(ConstantExpression expr) {
        if ( ! validateConstant(expr) ) {
            //errors << ["Invalid constant '${expr.value}'",expr]
            addError("Invalid constant '${expr.value}'",expr)
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
    protected boolean validateProperty(PropertyExpression prop) {
        def result = false
        if ( prop.property instanceof ConstantExpression && 
             prop.objectExpression instanceof VariableExpression &&
             prop.objectExpression.variable in owner.aliases
            ) 
        {
            result = true 
        }
        return result
        
    }
    
    void visitVariableExpression(VariableExpression expr) {
        if ( "this" == expr.name ) {
            return
        }
        if ( ! SqlVariables.isSupported(expr.name) ) {
            //errors << ["Unsupported variable  '${expr.name}'",expr]
            addError("Unsupported variable  '${expr.name}'",expr)
            return
        }

/*        whereList << new ConstructorCallExpression(
            ClassHelper.make(SqlVariableExpression),
            new TupleExpression(new NamedArgumentListExpression([
                        new MapEntryExpression(new ConstantExpression('name'),new ConstantExpression(expr.name))
                    ])))
*/                    
    }
    boolean isSupportedExpression(Expression expr) {
        return  expr instanceof BinaryExpression  ||
                expr instanceof VariableExpression  ||
                expr instanceof MethodCallExpression ||
                expr instanceof PropertyExpression ||
                expr instanceof ConstantExpression
        
    }
    void visitMethodCallExpression(MethodCallExpression expr) {
        //println "visitMethodCallExpression.method="  + expr.method.text  
        if ( ! SqlFunctions.isSupported(expr.method.text) ) {
            //errors << ["Unsupported function '${expr.method.text}'",expr]
            addError("Unsupported function '${expr.method.text}'",expr)
            return
        }
        
        if ( ! isSupportedExpression(expr.objectExpression) ) {
            //errors << ["Unsupported expression '${arg.text}' (method '${ownerCall.method.text}')",expr]
            addError("Unsupported expression '${arg.text}' (method '${ownerCall.method.text}')",expr)
            return
        }
        
        expr.objectExpression.visit(this)
        if ( errorFound()) {
            return
        }
        expr.arguments.visit(this)

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
        for (Expression expr : list) {
            if ( ! isSupportedExpression(expr) ) {
                //errors << ["UnsupportedExpression '${expr.text}'",expr]
                addError("UnsupportedExpression '${expr.text}'",expr)
                return
            }
            if (expr instanceof SpreadExpression) {
                Expression spread = ((SpreadExpression) expr).getExpression();
                spread.visit(this);
            } else {
                expr.visit(this);
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
    public void visitPropertyExpression(PropertyExpression expr) {
        if ( ! validateProperty(expr) ) {
            //errors << ["Invalid property expression '${expr.text}' in the method '${ownerCall.method.text}'",expr]
            addError("Invalid property expression '${expr.text}' in the method '${ownerCall.method.text}'",expr)
            return
        }
    
/*        expression.getObjectExpression().visit(this);
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
*/
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
            
    protected SourceUnit getSourceUnit() { owner.sourceUnit }
    
}

