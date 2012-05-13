package org.technbolts.jbehave.eclipse.editors.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;

public class NarrativeScanner extends AbstractStoryPartBasedScanner {
    
    private IToken keywordToken;

    public NarrativeScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        setDefaultToken(newToken(TextStyle.NARRATIVE_DEFAULT));
        keywordToken = newToken(TextStyle.NARRATIVE_KEYWORD);
    }
    
    @Override
    protected boolean isPartAccepted(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(keyword.isNarrative()) {
            return true;
        }
        return false;
    }

    @Override
    protected void emitPart(StoryPart part) {
        LocalizedStepSupport localizedStepSupport = getLocalizedStepSupport();
        if(handleKeyword(part, localizedStepSupport.lNarrative(false)) //
                || handleKeyword(part, localizedStepSupport.lAsA(false)) //
                || handleKeyword(part, localizedStepSupport.lInOrderTo(false)) //
                || handleKeyword(part, localizedStepSupport.lIWantTo(false))) {
            // done!
        }
        else {
            emitCommentAware(getDefaultToken(), part.getOffset(), part.getContent());
        }
    }
    
    private boolean handleKeyword(StoryPart part, String kwString) {
        String content = part.getContent();
        int offset = part.getOffset();
        
        if(content.startsWith(kwString)) {
            emit(keywordToken, offset, kwString.length());
            offset += kwString.length();
            emitCommentAware(getDefaultToken(), offset, content.substring(kwString.length()));
            return true;
        }
        return false;
    }
}
