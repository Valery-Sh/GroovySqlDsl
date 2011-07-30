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
import org.codehaus.groovy.ast.expr.DeclarationExpression
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
import org.codehaus.groovy.ast.expr.PostfixExpression
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
    def closure
    def rootExpression
    
    def methods = []
    def supportedMethods = ["select","from","join","leftjoin",
                            "rightjoin","fulljoin",
                            "on", "where","groupby",
                            "having","orderby"]

    def aliases = []
    
    public void visitClosureExpression(ClosureExpression expression) {
        closure = expression
        if ( closure.isParameterSpecified()) {
            
        }
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

        if ( ! validateMethods()) {
            return
        }
/*        def f = methods[0]
        def cast = f.getArguments().getExpression(0)
        def typ = cast.type
        println "TYPE:" + typ.text       
        def ve = new VariableExpression("me", typ)
        //def ve = new VariableExpression("me")
        def de = new DeclarationExpression(ve,new Token(Types.EQUAL,"=",-1,-1),new ConstantExpression(null))
        def es = new ExpressionStatement(de)
        def l = [es] + block.statements
        BlockStatement bs = new BlockStatement(l,block.variableScope)
        closure.setCode(bs)
        println "new STMT: " + closure.code.statements[0].getText()
*/        
    }
    def boolean validateMethods(){
        def result = true
        methods.each{
            switch(it.method.text.toLowerCase()) {
                case "select" : 
                    result = validateSelect(it)
                    break;
                case "from" : 
                case "join" : 
                case "leftjoin" : 
                case "rightjoin" : 
                case "fulljoin" : 
                    result = validateFrom(it)
                    break
                case "where" :
                    result = validateWhere(it)
                    break
                case "on" :
                    //result = validateWhere(it)
                    break
                case "orderby" :
                    result = validateOrderBy(it)
                    break
                case "groupby" :   
                    //result = validateGroupBy(it)
                    break
                case "having" :   
                    result = validateHaving(it)
                    break;
            }//switch
            if ( ! result ) {
                return false
            }
        }//each
        return result
        
    }
    /**
     * <ul>
     * <li>Must have at least one parameter</li>
     * <li>Each parameter may be:</li>
     * <ul>
     *  <li><code>VariableExpression</code></li> 
     *  <li><code>CastExpression</code></li> 
     * </ul>
     * </ul>
     * If a parameter is a <code>VariableExpression</code> then
     * it must be present in a closure parameter list. <p>
     *  
     */
    def boolean validateFrom(MethodCallExpression call) {
//        println "call.arguments.expressions=" + call.arguments.expressions.class
        if ( call.arguments.expressions.isEmpty() ) {
            addError("Method '${call.method.text}' must have at least one argument",call)
            return false
        }
        def alias
        call.arguments.expressions.each {
            if ( it instanceof VariableExpression ) {
                if ( ! isInClosureParemeters(it) ) {
                    addError("Closure parameters doesn't contain a parameter '${it.name}' (method '${call.method.text}')",it)
                    return false
                }
                alias = it.name
            } else if ( it instanceof CastExpression && it.expression instanceof VariableExpression) {
                alias = it.expression.name
            } else {
                addError("Unsupported parameter expression in the method '${call.method.text}'",it)
                return false
            }
            if ( alias in aliases ) {
                addError("Alias  '${alias}' is allready in use ",it)
                return false
            }
            aliases << alias
        }
        
        return true
        //if ( call instanceof )
    }
    
    def boolean validateOrderBy(MethodCallExpression call) {
        if ( call.arguments.expressions.isEmpty() ) {
            addError("Method '${call.method.text}' must have at least one argument",call)
            return false
        }
        call.arguments.expressions.each{
            def result = false
            if ( ! (it instanceof PropertyExpression)) {
                addError("Not a property expression '${it.text}' in the method '${call.method.text}'",call)
            } else if ( ! validateProperty(call,it)) {
                addError("Invalid property expression '${it.text}' in the method '${call.method.text}'",it)
            } else {
                result = true
            }
            return result
        }
        
    }
    
    def boolean validateProperty(MethodCallExpression call,PropertyExpression prop) {
        def result = false
        if ( prop.property instanceof ConstantExpression && 
             prop.objectExpression instanceof PropertyExpression &&
             (prop.property.value == "ASC" || 
              prop.property.value == "DESC") )
        {
            result = true
        } else if ( prop.property instanceof ConstantExpression && 
             prop.objectExpression instanceof VariableExpression &&
             prop.property.value != "ASC" && prop.property.value != "DESC") 
        {
            result = true 
        } else {
            addError("Invalid property expression '${prop.text}' in the method '${call.method.text}'",prop)
        }
        return result
        
    }
    def boolean isInClosureParemeters(variable) {
        def result = false
        closure.parameters.each {
            if ( it.name.equals(variable.name) ) {
                result = true
            }
        }
        return result
    }
    def validateMethodOrder() {

        def i = 0
        def c = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "select" ) {
            i++
            c++
        }
        if ( c > 1 ) {
            addError("Too many methods with a name 'select' ",methods[i-1])
            return false
        }
        
        c = 0
        while( i < methods.size() && methods[i].method.text.toLowerCase() == "from" ) {
            i++
            c++
        }
        if ( c == 0) {
            addError("A query must have a method 'from' ",closure)
            return false
            
        } else if ( c > 1 ) {
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
        
        c = 0
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
        return true
    }
    
    def boolean validateWhere(MethodCallExpression call) {
        def whereVisitor = new WhereValidateVisitor(owner:this,ownerCall:call)
        whereVisitor.startVisit()
        if ( ! whereVisitor.errors.isEmpty() ) {
println "WHERE V SIZE: " + whereVisitor.errors.size()           
println "WHERE V class: " + whereVisitor.errors[0].size()
            addError(whereVisitor.errors[0][0],whereVisitor.errors[0][1])
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

