package groovyx.sql.dsl.predef

/**
 *
 * @author V. Shyshkin
 */
class SqlFunctions {
    def functions = [ new SqlFunction(name:"UPPER",synonyms:["UCASE","TOUPPERCASE","TOUPPER"],
                        family:"string", types:[[String.class]]
        
        ]
        
                    
}//class

