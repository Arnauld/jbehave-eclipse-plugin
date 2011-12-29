package org.technbolts.jbehave.support;

import org.jbehave.core.configuration.Keywords;
import org.junit.Test;

public class JBKeywordTest {

    @Test
    public void listAll() {
        JBKeyword[] values = JBKeyword.values();
        for (int i = 0; i < values.length; i++) {
            System.out.println(values[i]+": ["+values[i].asString(new Keywords())+"]");
        }
    }
}
