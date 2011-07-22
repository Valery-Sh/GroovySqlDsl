package groovyx.sql.dsl.expr

/**
 *  Represents a result of a binary operation as an Expression.
 *  
 * @author V. Shyshkin
 */
class SqlBinaryExpression extends SqlExpression {
    def leftExpression
    def operation
    def rightExpression
    def SqlBinaryExpression(Map args) {
        super()
        println "SqlBinaryExpression constructor: "

        this.leftExpression = args["leftExpression"]        
        this.operation = args["operation"]
        this.rightExpression = args["rightExpression"]                
        println "SqlBinaryExpression.constructor left: " + leftExpression
        println "SqlBinaryExpression.constructor: " + rightExpression
    }
    
//println "SqlBinaryExpression: " + rightExpression.class    
    def String toString() {
//println "SqlBinaryExpression.toString() left: " + leftExpression.class            
//println "SqlBinaryExpression.toString() right: " + rightExpression.class            
        "(" + leftExpression + " " + operation + " " + rightExpression + ")"
    }
    
    def String shortInfo(int level) {
        def leftInfo
        def rightInfo
        
        if ( leftExpression instanceof SqlExpression ) {
            leftInfo = leftExpression.shortInfo(level+1)
        } else {
            leftInfo = leftExpression.toString()
        }

        if ( rightExpression instanceof SqlExpression ) {
            rightInfo = rightExpression.shortInfo(level+1)
        } else {
            rightInfo = rightExpression.toString()
        }

        "----" * level + "class: " + this.class.simpleName + "\n" + 
        leftInfo + 
        rightInfo + "\n"
        
        
//        + leftInfo + "\n"
        //+ rightInfo + "\n"
//        +
//         "----" * (curLevel + 1) + " leftExpr: " + leftInfo + "\n" +
//         "----" * (curLevel + 1) + " operation: " + operation + "\n" +
//         "----" * (curLevel + 1) + " rightExpr: " + rightInfo + "\n" 
    }
    
}

