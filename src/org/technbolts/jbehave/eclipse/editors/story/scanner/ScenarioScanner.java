package org.technbolts.jbehave.eclipse.editors.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;

public class ScenarioScanner extends AbstractStoryPartBasedScanner {
    
    private IToken keywordToken;

    public ScenarioScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        setDefaultToken(newToken(TextStyle.SCENARIO_DEFAULT));
        keywordToken = newToken(TextStyle.SCENARIO_KEYWORD);
    }
    
    @Override
    protected boolean isPartAccepted(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(keyword==JBKeyword.Scenario || keyword.isComment()) {
            return true;
        }
        return false;
    }

    @Override
    protected void emitPart(StoryPart part) {
        String content = part.getContent();
        String kwString = getLocalizedStepSupport().lScenario(false);
        int offset = part.getOffset();
        
        if(content.startsWith(kwString)) {
            emit(keywordToken, offset, kwString.length());
            offset += kwString.length();
            emitCommentAware(getDefaultToken(), offset, content.substring(kwString.length()));
        }
        else {
            emitCommentAware(getDefaultToken(), offset, content);
        }
    }
}
