/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.temp
import groovyx.sql.dsl.adapter.*


def sqlAdapter = new SqlAdapter()
def m1 = 3

def sqlBuilder = sqlAdapter.query {
     //where 5 < 12 && 7 == 9 && a like b || param0.toUpperCase() == "shyshkin" && 2 < 4
    //where SESSION_USER < CURRENT_USER && 14 < 89
    //where  11 > 1 && 14 < 89 && !(12 > 2 || 15 < 90)
    //where 5 > 4 || "a008".upper(123) < 10
 //   where 5 > 4.upper(555) || "10" < "a008".lower(666)
 
//    from p in Person, o == any( select all fro  d()) 
//    
//    where !((1 + 2*3).upper("aa21".lower(m2).myMeth(m1)) > ! myvar || c.v.d && u in t)
where a > 5.upper()
        //where !( SESSION_USER == 7 && (  !(5 >= "a008".upper(123)) || 15 < 90 && !(12 > 2)) )
    
}
/*sqlBuilder.exprList.each {
    println it
}
*/
//println " ======================================= " + 'bbbb'.'toUpperCase'()
//println "sqlBuilder.class = " + sqlBuilder.whereExpr.class
println "WhereExpr : ---------  " + sqlBuilder.whereExpr

println "" + sqlBuilder.whereExpr.shortInfo(0)



