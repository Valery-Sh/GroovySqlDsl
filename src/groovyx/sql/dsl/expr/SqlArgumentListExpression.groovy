package groovyx.sql.dsl.expr

/**
 *  Represents an argument list in a sql function call.
 *  
 * @author V. Shyshkin
 */
class SqlArgumentListExpression extends SqlExpression {
    def arguments = []
    SqlArgumentListExpression(Map args) {
        super()    
        this.arguments = args["arguments"]
println "SqlArgumentListExpression.arguments.class="  + arguments.class      
    }
    String toString() {
        println "SqlArgumentListExpression: " 
        def comma = ""
        def i = 0
        arguments.each{
            println "SqlArgumentListExpression.arguments.each="  + (i++)
            if ( it != null )
                return comma + it.toString()
            else 
                return comma + "null"
            comma = ","    
        }
    }
}

