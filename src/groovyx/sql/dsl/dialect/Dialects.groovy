/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.dialect

/**
 *
 * @author Valery
 */
abstract class Dialects {
    //private static ServiceLoader<DialectProvider> dialectLoader = ServiceLoader.load(DialectProvider.class)
     
    public static Dialect getDialect(String dialectName, ClassLoader loader) {
        ServiceLoader<DialectProvider> dialectLoader = ServiceLoader.load(DialectProvider.class,loader)
        for (DialectProvider provider : dialectLoader) {
            Dialect dp = provider.getDialect(dialectName);
            if (dp != null)
            return dp;
        }
        return null;
    }
    public static Dialect getDialect(String dialectName) {
        ServiceLoader<DialectProvider> dialectLoader = ServiceLoader.load(DialectProvider.class)
        for (DialectProvider provider : dialectLoader) {
            Dialect dp = provider.getDialect(dialectName);
            if (dp != null)
            return dp;
        }
        return null;
    }
    
}

