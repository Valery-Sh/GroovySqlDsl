package groovyx.sql.dsl.adapter

import groovyx.sql.dsl.expr.builder.*
/**
 *
 * @author V. Shyshkin
 */
class SqlAdapter {
    def query(Closure closure) {
        println "(-- 2 --)"

        Closure clon = closure.clone()
        clon.resolveStrategy = Closure.DELEGATE_FIRST
        def builder = new SqlBuilder()
        clon.delegate = builder
        clon()
        return builder.createQuery()
        
    }
}

