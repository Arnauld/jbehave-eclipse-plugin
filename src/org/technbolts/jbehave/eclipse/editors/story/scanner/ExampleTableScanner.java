package org.technbolts.jbehave.eclipse.editors.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.JBPartition;

public class ExampleTableScanner extends AbstractStoryPartBasedScanner {
    
    private IToken keywordToken;

    public ExampleTableScanner(TextAttributeProvider textAttributeProvider) {
        super(textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        setDefaultToken(newToken(TextStyle.EXAMPLE_TABLE_DEFAULT));
        keywordToken = newToken(TextStyle.EXAMPLE_TABLE_KEYWORD);
        exampleTableCellToken = newToken(TextStyle.EXAMPLE_TABLE_CELL);
        exampleTableSepToken  = newToken(TextStyle.EXAMPLE_TABLE_SEPARATOR);
    }
    
    @Override
    protected boolean isPartAccepted(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(JBPartition.ExampleTable==JBPartition.partitionOf(keyword)) {
            return true;
        }
        return false;
    }

    @Override
    protected void emitPart(StoryPart part) {
        String content = part.getContent();
        String kwString = JBKeyword.ExamplesTable.asString();
        int offset = part.getOffset();
        
        if(content.startsWith(kwString)) {
            emit(keywordToken, offset, kwString.length());
            offset += kwString.length();
            emitTable(getDefaultToken(), offset, content.substring(kwString.length()));
        }
        else {
            emit(getDefaultToken(), offset, content.length());
        }
    }


}
