package org.technbolts.jbehave.eclipse.editors.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;

public class ScenarioScanner extends AbstractStoryPartBasedScanner {
    
    private IToken keywordToken;

    public ScenarioScanner(TextAttributeProvider textAttributeProvider) {
        super(textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        setDefaultToken(newToken(TextStyle.SCENARIO_DEFAULT));
        keywordToken = newToken(TextStyle.SCENARIO_KEYWORD);
    }
    
    @Override
    protected boolean isPartAccepted(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(keyword==JBKeyword.Scenario) {
            return true;
        }
        return false;
    }

    @Override
    protected void emitPart(StoryPart part) {
        String content = part.getContent();
        String kwString = JBKeyword.Scenario.asString();
        int offset = part.getOffset();
        
        if(content.startsWith(kwString)) {
            emit(keywordToken, offset, kwString.length());
            offset += kwString.length();
            emit(getDefaultToken(), offset, content.length()-kwString.length());
        }
        else {
            emit(getDefaultToken(), offset, content.length());
        }
    }
}
