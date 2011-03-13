package org.technbolts.jbehave.support;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.technbolts.util.CharTree;

public class JBehaveContext {
    
    private static JBehaveContext context;
    public static JBehaveContext get() {
        if(context==null)
            context = new JBehaveContext();
        return context;
    }
    
    private CharTree<JBKeyword> keywordTree;
    private Keywords keywords;
    
    public Keywords getKeywords() {
        if(keywords==null)
            keywords = new LocalizedKeywords();
        return keywords;
    }
    
    public CharTree<JBKeyword> getKeywordTree() {
        if(keywordTree==null) {
            Keywords kws = getKeywords();
            keywordTree = new CharTree<JBKeyword>('/', null);
            for(JBKeyword kw : JBKeyword.values()) {
                keywordTree.push(kw.asString(kws), kw);
            }
        }
        return keywordTree;
    }
    
}
