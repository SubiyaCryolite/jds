package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsImplementation;

/**
 * Created by ifunga on 14/07/2017.
 */
public abstract class JdsDbOracle extends JdsDbPostgreSql {

    public JdsDbOracle()
    {
        supportsStatements = true;
        implementation = JdsImplementation.ORACLE;
    }
}
