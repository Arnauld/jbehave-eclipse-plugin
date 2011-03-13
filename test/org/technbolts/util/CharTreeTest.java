package org.technbolts.util;

import static org.junit.Assert.assertEquals;
import static org.technbolts.jbehave.support.JBKeyword.*;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.technbolts.jbehave.support.JBKeyword;

public class CharTreeTest {
    
    private CharTree<JBKeyword> cn;

    @Before
    public void setUp () {
        LocalizedKeywords keywords = new LocalizedKeywords();
        cn = new CharTree<JBKeyword>('/', null);
        for(JBKeyword kw : JBKeyword.values())
            cn.push(kw.asString(keywords), kw);
        
    }
    
    @Test
    @Ignore
    public void print () {
        cn.print(0);
    }
    
    @Test
    public void lookup() {
        assertEquals(Given, cn.lookup("Given"));
        assertEquals(Narrative, cn.lookup("Narrative:"));
        assertEquals(Given, cn.lookup("Given a user named \"Bob\""));
        assertEquals(InOrderTo, cn.lookup("In order to be more communicative"));
    }
}
