
package groovyx.sql.dsl.expr

/**
 * Contains a name of the SQL predifined constant
 * @author V. Shyshkin
 */
class SqlConstantExpression extends SqlExpression {
    def value
    String toString() {
        if ( value != null && (value instanceof String) ) {
            return "'" + value + "'"
        } else {
            return value == null ? null : value.toString()
        }
    }
    String shortInfo(int level) {
        "----" * level + "class: " + this.class.simpleName + "\n"
//        "----" * (level+1) + "      value: " + value + "\n"
    }
}

