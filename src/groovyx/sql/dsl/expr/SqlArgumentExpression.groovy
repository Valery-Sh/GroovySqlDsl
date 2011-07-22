/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.expr

/**
 *
 * @author Valery
 */
class SqlArgumentExpression extends SqlExpression{
    def argument
    def SqlArgumentExpression(Map args) {
        argument = args["argument"]
    }
    String toString() {
        if ( argument != null )
            return argument.toString()
        else 
            return "null"
    }
}

