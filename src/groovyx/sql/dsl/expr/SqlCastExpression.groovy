/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.expr

/**
 *
 * @author Valery
 */
class SqlCastExpression {
    def expression
    def type
    String toString(){
        "" + expression + " as " + type
    }
}

