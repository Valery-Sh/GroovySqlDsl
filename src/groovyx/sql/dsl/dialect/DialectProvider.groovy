/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.dialect

/**
 *
 * @author Valery
 */
abstract class DialectProvider {
    abstract Dialect getDialect(String dialectName)
}

