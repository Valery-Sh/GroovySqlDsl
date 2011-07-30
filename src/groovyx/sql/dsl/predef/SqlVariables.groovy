package groovyx.sql.dsl.predef

/**
 *
 * @author V. Shyshkin
 */
class SqlVariables {
    static variables = [new SqlVariable(name:'SESSION_USER',type:String.class,synonyms:[]),
                        new SqlVariable(name:'CURRENT_USER',type:String.class,synonyms:[]),
                        new SqlVariable(name:'SYSTEM_USER',type:String.class,synonyms:[])
                       ]
    
    static isSupported(v) {
        def result = false
        variables.each{
            if ( v.toUpperCase() == it.name ) {
                result = true
            }
        }
        return result
    }
    
}

