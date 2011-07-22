package groovyx.sql.dsl.expr

/**
 * Contains SQL predifined variable name
 * @author V. Shyshkin
 */
class SqlVariableExpression extends SqlExpression {
    String name
    String toString() {
        name
    }
    String shortInfo(int level) {
        "----" * level + "class: " + this.class.simpleName + "\n"
//        "----" * (level+1) + "      name: " + name + "\n"
    }
    
}

