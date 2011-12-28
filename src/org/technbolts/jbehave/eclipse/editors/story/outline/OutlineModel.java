package org.technbolts.jbehave.eclipse.editors.story.outline;

import org.technbolts.jbehave.support.JBKeyword;

public class OutlineModel {

    private JBKeyword keyword;
    private String content;
    private int offset, length;
    
    public OutlineModel(JBKeyword keyword, String content, int offset, int length) {
        super();
        this.keyword = keyword;
        this.content = content;
        this.offset = offset;
        this.length = length;
    }

    public JBKeyword getKeyword() {
        return keyword;
    }
    
    public String getContent() {
        return content;
    }
    
    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
    
}
