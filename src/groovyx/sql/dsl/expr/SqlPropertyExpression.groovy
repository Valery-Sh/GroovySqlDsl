package groovyx.sql.dsl.expr

/**
 *
 * @author V. Shyshkin
 */
class SqlPropertyExpression {
    def object
    def property
    def String toString() {
        "" + object + "." + property
    }
}

