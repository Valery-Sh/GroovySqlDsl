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
    def String shortInfo(int level) {
        def objInfo
        def propInfo
        if ( object instanceof SqlExpression ) {
            objInfo = object.shortInfo(level+1)
        } else {
            objInfo = object.toString()
        }
        if ( property instanceof SqlExpression ) {
            propInfo = property.shortInfo(level+1)
        } else {
            propInfo = property.toString()
        }
        
        "----" * level + "class: " + this.class.simpleName + "\n" + 
        objInfo + 
        propInfo + "\n"        
    }
    
}

