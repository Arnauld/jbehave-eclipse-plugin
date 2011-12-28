package org.technbolts.jbehave.parser;

import java.util.Locale;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.CharTree;

public class Constants {
    private static final CharTree<JBKeyword> kwTree = createKeywordCharTree();
    
    public static CharTree<JBKeyword> sharedKeywordCharTree() {
        return kwTree;
    }
    
    public static CharTree<JBKeyword> createKeywordCharTree() {
        LocalizedKeywords keywords = new LocalizedKeywords(Locale.US);
        CharTree<JBKeyword> cn = new CharTree<JBKeyword>('/', null);
        for(JBKeyword kw : JBKeyword.values()) {
            String asString = kw.asString(keywords);
            cn.push(asString, kw);
        }
        return cn;
    }
}
