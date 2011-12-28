package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.jbehave.parser.StoryParser;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartVisitor;
import org.technbolts.jbehave.support.JBPartition;
import org.technbolts.util.New;

public class StoryPartitionScanner implements org.eclipse.jface.text.rules.IPartitionTokenScanner {

    private IDocument document;
    //
    private int cursor;
    private Partition currentPartition;
    private List<Partition> partitions;
    
    public StoryPartitionScanner() {
    }
    
    @Override
    public void setRange(IDocument document,
            int offset,
            int length) {
        setPartialRange(document, offset, length, null, -1);
    }
    
    @Override
    public void setPartialRange(IDocument document,
            int offset,
            int length,
            String contentType,
            int partitionOffset) {
        System.out.println("\n\n==================================\nStoryPartitionScanner.setPartialRange(\n" + 
                "offset...........: " + offset + "\n" +
                "length...........: " + length + "\n" +
                "contentType......: " + contentType + "\n" + 
                "partitionOffset..: " + partitionOffset + "\n==================================\n");
        
        this.document = document;
        initializePartitions();
    }
    
    @Override
    public int getTokenLength() {
        return currentPartition.length;
    }
    
    @Override
    public int getTokenOffset() {
        return currentPartition.offset;
    }
    
    @Override
    public IToken nextToken() {
        IToken token = nextToken0();
        System.out.println("StoryPartitionScanner.nextToken(" + token.getData() + ", currentPartition: " + currentPartition + ")");
        return token;
    }
    
    private IToken nextToken0 () {
        if(cursor<partitions.size()) {
            currentPartition = partitions.get(cursor++);
            return new Token(currentPartition.keyword.name());
        }
        return Token.EOF;
    }

    private void initializePartitions() {
        partitions = New.arrayList();
        cursor = 0;
        
        String content = document.get();
        new StoryParser().parse(content, new StoryPartVisitor() {
            @Override
            public void visit(StoryPart part) {
                push(part);
            }
        });
        
        for(Partition p : partitions) {
            System.out.println(">> " + p.keyword + ", offset:" + p.offset + ", length: " + p.length);
        }
    }
    
    private void push(StoryPart part) {
        Partition p = new Partition(
                JBPartition.partitionOf(part.getKeyword()),
                part.getOffset(),
                part.getLength());
        
        if(partitions.isEmpty()) {
            partitions.add(p);
            return;
        }
        
        // pick last, merge it or add it to the list
        Partition last = partitions.get(partitions.size()-1);
        if(!last.merge(p))
            partitions.add(p);
    }
    
    private class Partition {
        private JBPartition keyword;
        private int offset;
        private int length;
        public Partition(JBPartition keyword, int offset, int length) {
            this.keyword = keyword;
            this.offset = offset;
            this.length = length;
        }
        public boolean merge(Partition p) {
            if(keyword==p.keyword) {
                this.length += p.length;
                return true;
            }
            return false;
        }
        @Override
        public String toString() {
            return "P["+keyword+", offset: " + offset + ", length: " + length + "]";
        }
    }
    
}
