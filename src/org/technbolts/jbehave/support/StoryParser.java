package org.technbolts.jbehave.support;

import static org.technbolts.jbehave.support.JBPartition.partitionOf;

import org.technbolts.util.BidirectionalReader;
import org.technbolts.util.CharTree;

public class StoryParser {
    
    private CharTree<JBKeyword> charTree;
    protected boolean partition;
    
    public StoryParser(boolean partition) {
        this.partition = partition;
        this.charTree = JBehaveContext.get().getKeywordTree();
    }
    
    public JBKeyword nextKeyword(BidirectionalReader scanner) {
        int readCount = 0;
        JBKeyword keyword = null;
        StringBuilder line = null;
        while(!scanner.eof()) {
            int posBeforeLine = scanner.getPosition();
            line = scanner.readLine();
            
            System.out.println("StoryParser.nextKeyword(1:line: " + line + ", pos: " + posBeforeLine + ")");
            
            try {
                JBKeyword keywordRead = extractKeyword(line);
                System.out.println("StoryParser.nextKeyword(2: keywordRead:" + keywordRead + ", keyword:" + keyword + ")");
                if(keywordRead==null) {
                    // no stop word, data can extends to multiple line, continue :)
                    continue;
                }
                else
                if(keyword==null) {
                    System.out.println("StoryParser.nextKeyword(3.1:keyword is null, readCount: " + readCount + ")");
                    // no keyword yet, if something has been read then it is undefined
                    if(readCount>0) {
                        System.out.println("StoryParser.nextKeyword(3.2:backToPosition: " + posBeforeLine);
                        new Exception().printStackTrace();
                        scanner.backToPosition(posBeforeLine);
                        return null;
                    }
                    else
                        keyword = keywordRead;
                }
                else if(isKeywordStop(keywordRead, keyword)) {
                    System.out.println("StoryParser.nextKeyword(3:--)");

                    // already within a keyword, thus it is a stop word
                    scanner.backToPosition(posBeforeLine);
                    return keyword;
                }
            
            } 
            finally {
                readCount += line.length();
            }
        }
        
        System.out.println("StoryParser.nextKeyword(end: " + keyword);
        
        // remaining
        return keyword;
    }
    
    //
    public JBKeyword extractKeyword(CharSequence line) {
        return charTree.lookup(line);
    }
    
    protected boolean isKeywordStop(JBKeyword keywordRead, JBKeyword keyword) {
        if(partition) {
            return partitionOf(keyword) != partitionOf(keywordRead);
        }
        else 
            return true;
    }

}
