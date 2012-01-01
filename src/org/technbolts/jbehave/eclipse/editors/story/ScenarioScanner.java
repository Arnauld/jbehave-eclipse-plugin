package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;

public class ScenarioScanner extends AbstractStoryPartBasedScanner {
    
    private IToken keywordToken;

    public ScenarioScanner(TextAttributeProvider textAttributeProvider) {
        
        TextAttribute textAttribute = textAttributeProvider.get(StoryTextAttributes.Scenario);
        setDefaultToken(new Token(textAttribute));
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.ScenarioKeyword);
        keywordToken = new Token(textAttribute);
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
