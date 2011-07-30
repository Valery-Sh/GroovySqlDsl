/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.predef

/**
 *
 * @author V. Shyshkin
 */
class SqlVariable {
    def name
    def synonyms = []
    def type
    
    boolean isSupported(String v) {
        if ( name == v.toUpperCase() || (v.toUpperCase() in synonyms)) {
            return true
        } 
        return false
    }
	
}

