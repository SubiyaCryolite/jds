package org.jenesis.jds;

import java.util.HashMap;

/**
 * Created by ifung on 03/03/2017.
 */
public class JdsQuery {

    private final HashMap<JdsField, Object> andQueries;
    private final HashMap<JdsField, Object> orQueries;

    public JdsQuery() {
        andQueries = new HashMap<>();
        orQueries = new HashMap<>();
    }

    public JdsQuery where(JdsField jdsField, Object value) {
        if (!andQueries.containsKey(jdsField)) {
            andQueries.put(jdsField, value);
        }
        return this;
    }
}

