package groovyx.sql.dsl.expr.builder

import groovyx.sql.dsl.expr.*
/**
 * Creates sql query string expression.
 * Serves as a delegate object to execute a query closure.
 * @author V. Shyshkin
 */
class SqlBuilder {
    def whereExpr 
    def exprList = [] 
    
    def where(expr) {
//println " whereExpr.class = "  + expr.class      
        whereExpr = expr
        exprList << "WHERE"
        return this
    }
    def join(expr) {
        //whereExpr = expr
        exprList << "JOIN"
        return this
    }
    
    def createQuery() {
       this 
    }

}

