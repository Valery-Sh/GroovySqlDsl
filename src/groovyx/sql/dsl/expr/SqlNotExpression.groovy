package groovyx.sql.dsl.expr

/**
 * Represents a sql NOT expression
 * @author V. Shyshkin
 */
class SqlNotExpression extends SqlExpression {
    def expression
    def String toString() {
        "!" + expression
    }
}

