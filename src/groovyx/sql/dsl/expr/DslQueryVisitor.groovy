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
    def supportedMethods = ["from","join","leftjoin",
                            "rightjoin","fulljoin",
                            "on", "where","groupby",
                            "having","orderby"]
    
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
            
            def list = []
            
            def object = it.expression
            
            while (! (object instanceof VariableExpression && "this" == object.text)) {
                if ( object instanceof MethodCallExpression && object.method.text.toLowerCase() in supportedMethods ) {
                    list << object
                    object = object.objectExpression
                } else if ( object instanceof MethodCallExpression) {
                    msg = "Unsupported method name '${object.method.text}'"
                    addError(msg,object)
                    return
                } else {
                    addError(msg,object)
                    return
                }
            }//while
            //methods << list.reverse()
            methods += list.reverse()
        }//each

        methods.each {
            println it.method.text
        }
        
        if ( !validateMethodOrder() ) {
            return
        }
    }
    
    def validateMethodOrder() {

        def i = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "from" ) {
            i++
        }
        if ( i == 0) {
            addError("A query must have a method 'from' ",methods[0])
            return false
            
        } else if ( i > 1 ) {
            addError("Too many methods with a name 'from' ",methods[i-1])
            return false
            
        }

        while( i < methods.size() && methods[i].method.text.toLowerCase().endsWith("join") ) {
            def j = ++i
            while ( j < methods.size() && methods[j].method.text.toLowerCase() == "on") {
                j++
            }
            if ( j - i > 1 ) {
                addError("Too many methods with a name 'on' ",methods[i])
                return false
            }
            i = j
        }
        
        def c = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "where" ) {
            i++
            c++
        }
        if ( c > 1 ) {
            addError("Too many methods with a name 'where' ",methods[i-1])
            return false
        }
        c = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "groupby" ) {
            i++
            c++
        }
        if ( c > 1 ) {
            addError("Too many methods with a name 'groupBy' ",methods[i-1])
            return false
        }
        c = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "having" ) {
            i++
            c++
        }
        if ( c > 1 ) {
            addError("Too many methods with a name 'having' ",methods[i-1])
            return false
        }
        c = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "orderby" ) {
            i++
            c++
        }
        if ( c > 1 ) {
            addError("Too many methods with a name 'orderBy' ",methods[i-1])
            return false
        }
        
        if ( i < methods.size() ) {
            addError("Unexpected method with a name '${methods[i].method.text}' ",methods[i])
            return false
            
        }
        
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

