/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.predef

/**
 *
 * @author Valery
 */
class SqlFunction {
    def name
    def synonyms = []
    def argTypes = []
    def String family
    
    boolean isSupported(String fn) {
        if ( name == fn.toUpperCase() || (fn.toUpperCase() in synonyms)) {
            return true
        } 
        return false
    }
    boolean isSupported(String fn, List<Class> types) {
        
        if ( ! isSupported(fn)) {
            return false
        } 
        if ( argTypes.isEmpty() != types.isEmpty() ) {
            return false
        }
        if ( ! (types in argTypes) ) {
            return false
        }
        return true
    }
    
}

