/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovyx.sql.dsl.predef

/**
 *
 * @author V. Shyshkin
 */
enum SqlFunctions {
    // ======= String Functions ==================
    SUBSTRING,        // substring('Valery' from 2 for 3)
    SIMILAR,          // substring(‘antidisestablishmentarianism’  SIMILAR ‘antidis\”[:ALPHA:]+\”arianism’ ESCAPE ‘\’ )
    UPPER,     
    LOWER,
    TRIMLEADING,      // TRIM (LEADING ‘A’ FROM ‘ALERT’)
    TRIMBOTH,         // TRIM (BOTH ‘ ‘ FROM ‘ ALERT ‘)  
    TRIMTRAILING,     // TRIM (TRAILING ‘ ‘ FROM ‘ ALERT ‘) 
    TRANSLATE,
    CONVERT,
    OVERLAY,          // OVERLAY (‘I Love Paris’ PLACING ‘Tokyo’ FROM 8 FOR 5)
    
    // ======= Numeric value functions ==================    
    
    POSITION,         // POSITION (‘au’ IN  ' automatic’)      
    EXTRACT,          // EXTRACT (MONTH FROM DATE ‘2007-12-04’)
    CHARACTER_LENGTH, //CHARACTER_LENGTH (‘Transmission, manual’)
    OCTET_LENGTH,     // OCTET_LENGTH (‘Brakes, disc’)
    CARDINALITY,      // CARDINALITY (TeamRoster)
    ABS,              // ABS (-273)
    MOD,              // MOD (6,4)
    
    // ======= Date Time value functions ==================    
    
    CURRENT_DATE,     // CURRENT_DATE 2011-01-23
    CURRENT_TIME,     // CURRENT_TIME (1) 08:36:57.3
    CURRENT_TIMESTAMP// CURRENT_TIMESTAMP (2) 2011-01-23 08:36:57.38    
    
}

