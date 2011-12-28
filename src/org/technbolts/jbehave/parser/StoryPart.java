package org.technbolts.jbehave.parser;

import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.CharTree;

public class StoryPart {
    private final int offset;
    private final String content;
    
    public StoryPart(int offset, String content) {
        super();
        this.offset = offset;
        this.content = content;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public int getLength() {
        return content.length();
    }
    
    public String getContent() {
        return content;
    }
    
    public JBKeyword getKeyword() {
        return getKeyword(defaultTree());
    }
    
    public JBKeyword getKeyword(CharTree<JBKeyword> kwTree) {
        return kwTree.lookup(getContent());
    }
    
    public boolean startsWithKeyword () {
        return startsWithKeyword(defaultTree());
    }

    public boolean startsWithKeyword (CharTree<JBKeyword> kwTree) {
        return (getKeyword()!=null);
    }
    
    private CharTree<JBKeyword> defaultTree() {
        return Constants.sharedKeywordCharTree();
    }

    public int getOffsetEnd() {
        return getOffset()+getLength();
    }

}
