package org.technbolts.jbehave.eclipse.editors.story.outline;

import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.JBPartition;

public class OutlineModel {

    private JBPartition partition;
    private String content;
    private int offset, length;
    
    public OutlineModel(JBKeyword keyword, String content, int offset, int length) {
        super();
        this.partition = JBPartition.partitionOf(keyword);
        this.content = content;
        this.offset = offset;
        this.length = length;
    }
    
    boolean merge(OutlineModel model) {
        if(partition==model.partition) {
            // make sure there is no hole: otherwise the model must be emitted
            if(this.offset+this.length == model.offset) {
                this.length += model.length;
                return true;
            }
        }
        return false;
    }

    public JBPartition getPartition() {
        return partition;
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
