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
        while(!scanner.eof()) {
            int posBeforeLine = scanner.getPosition();
            StringBuilder line = scanner.readLine();
            
            try {
                JBKeyword keywordRead = extractKeyword(line);
                if(keywordRead==null) {
                    // no stop word, data can extends to multiple line, continue :)
                    continue;
                }
                else
                if(keyword==null) {
                    // no keyword yet, if something has been read then it is undefined
                    if(readCount>0)
                        return keyword;
                    else
                        keyword = keywordRead;
                }
                else if(isKeywordStop(keywordRead, keyword)) {
                    // already within a keyword, thus it is a stop word
                    scanner.backToPosition(posBeforeLine);
                    return keyword;
                }
            
            } 
            finally {
                readCount += line.length();
            }
        }
        
        // remaining
        return keyword;
    }
    
    public JBKeyword extractKeyword(StringBuilder line) {
        return charTree.lookup(line);
    }
    
    protected boolean isKeywordStop(JBKeyword keywordRead, JBKeyword keyword) {
        if(partition)
            return partitionOf(keyword) != partitionOf(keywordRead);
        else 
            return keyword != keywordRead;
    }
}
