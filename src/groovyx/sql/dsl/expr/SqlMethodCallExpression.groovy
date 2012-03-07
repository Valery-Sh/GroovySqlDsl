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
}

