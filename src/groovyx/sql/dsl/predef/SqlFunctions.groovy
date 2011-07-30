package groovyx.sql.dsl.predef

/**
 *
 * @author V. Shyshkin
 */
class SqlFunctions {
    static functions = [ new SqlFunction(name:"UPPER",synonyms:["UCASE","TOUPPERCASE","TOUPPER"],
                        family:"string", argTypes:[[String.class]])
        
        ]
    
    static boolean isSupported(String fn) {
        def result = false
        functions.each {
            if ( it.isSupported(fn) ) {
                result = true
            }
        }
        return result
    }    
                    
}//class

