package org.technbolts.jbehave.eclipse.editors.story.outline;

import java.util.ArrayList;
import java.util.List;

import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.JBPartition;

public class OutlineModel {

    private final JBPartition partition;
    private JBKeyword keyword;
    private String content;
    private int offset, length;
    private List<OutlineModel> children = new ArrayList<OutlineModel>();
    
    public OutlineModel(JBKeyword keyword, String content, int offset, int length) {
        super();
        this.keyword = keyword;
        this.partition = JBPartition.partitionOf(keyword);
        this.content = extractSingleLine(content);
        this.offset = offset;
        this.length = length;
    }
    
    private static String extractSingleLine(String content) {
        String[] lines = content.split("[\r\n]+");
        for(String line : lines) {
            if(!line.trim().isEmpty())
                return line;
        }
        return null;
    }
    
    public JBKeyword getKeyword() {
        return keyword;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public List<OutlineModel> getChildren() {
        return children;
    }
    
    boolean merge(OutlineModel model) {
        if(partition==model.partition) {
            // make sure there is no hole: otherwise the model must be emitted
            if(this.offset+this.length == model.offset) {
                appendChild(model);
                this.length += model.length;
                return true;
            }
        }
        return false;
    }

    private void appendChild(OutlineModel model) {
        if(children.isEmpty()) {
            // add self as child
            // children.add(copy());
        }
        children.add(model);
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
