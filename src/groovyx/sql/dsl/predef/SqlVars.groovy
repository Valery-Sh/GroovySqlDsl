package groovyx.sql.dsl.predef

/**
 *
 * @author V. Shyshkin
 */
class SqlVars {
    static predifined = ['SESSION_USER','CURRENT_USER','SYSTEM_USER']
    static contains(v) {
        if ( v in predifined ) {
            return true
        }
        return false
    }
    
}

