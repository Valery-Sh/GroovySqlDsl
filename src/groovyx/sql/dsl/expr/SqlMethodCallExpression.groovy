/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.expr

/**
 *
 * @author Valery
 */
class SqlMethodCallExpression extends SqlExpression  {
    def object
    def method
    def arguments
    SqlMethodCallExpression(Map args) {
        super()
        this.object = args["object"]
        this.method = args["method"]
        this.arguments = args["arguments"]
    }
    String toString() {
        println "SqlMethodCallExpression.arguments.class = " + arguments.class
        println "SqlMethodCallExpression.arguments = " + arguments
        //println "SqlMethodCallExpression.arguments[0].class = " + arguments[0].class
        //println "SqlMethodCallExpression.arguments[0] = " + arguments[0]
        
        "" + object + "." + method + "(" + arguments + ")"
    }
    def String shortInfo(int level) {
        def objInfo
        def methodInfo
        def argInfo
        if ( object instanceof SqlExpression ) {
            objInfo = object.shortInfo(level+1)
        } else {
            objInfo = "----" * (level+1) + object.toString() + "\n"
        }
        if ( method instanceof SqlExpression ) {
            methodInfo = method.shortInfo(level+1)
        } else {
            methodInfo = "----" * (level+1) + method.toString() + "\n"
        }
        
        if ( arguments instanceof SqlExpression ) {
            argInfo = arguments.shortInfo(level+1)
        } else {
            argInfo = "----" * (level+1) + arguments.toString()  + "\n"        
        }
        
        "----" * level + "class: " + this.class.simpleName + "\n" + 
        objInfo + 
        methodInfo +
        argInfo + "\n"        
    }
    
    
}

