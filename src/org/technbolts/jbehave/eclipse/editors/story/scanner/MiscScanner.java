package org.technbolts.jbehave.eclipse.editors.story.scanner;

import static org.technbolts.jbehave.support.JBKeyword.GivenStories;
import static org.technbolts.jbehave.support.JBKeyword.Meta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.IToken;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.parser.Constants;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.Constants.TokenizerCallback;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.JBPartition;

public class MiscScanner extends AbstractStoryPartBasedScanner {
    
    private IToken keywordToken;
    private IToken metaPropertyToken;

    public MiscScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        setDefaultToken(newToken(TextStyle.META_DEFAULT));
        keywordToken = newToken(TextStyle.META_KEYWORD);
        metaPropertyToken = newToken(TextStyle.META_KEYWORD);
    }
    
    @Override
    protected boolean isPartAccepted(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(JBPartition.Misc==JBPartition.partitionOf(keyword)) {
            return true;
        }
        return false;
    }
    
    private boolean handle(StoryPart part, JBKeyword kw, IToken token, Chain chain) {
        String content = part.getContent();
        String kwString = kw.asString(getLocalizedStepSupport().getLocalizedKeywords());
        if(content.startsWith(kwString)) {
            int length = kwString.length();
            int offset = part.getOffset();
            emit(token, offset, length);
            offset += length;
            
            chain.next(offset, content.substring(length));
            return true;
        }
        return false;
    }

    @Override
    protected void emitPart(StoryPart part) {
        Chain commentAwareChain = commentAwareChain(getDefaultToken());
        if(handle(part, GivenStories, keywordToken, commentAwareChain)
                || handle(part, Meta, keywordToken, metaChain())) {
            // nothing more to do?
        }
        else {
            emitCommentAware(getDefaultToken(), part.getOffset(), part.getContent());
        }
    }

    private Chain metaChain() {
        return new Chain() {
            @Override
            public void next(int offset, String content) {
                parseMetaProperties(offset, content);
            }
        };
    }
    
    private static Pattern metaProperty = Pattern.compile("\\s*@\\s*[^\\s]+");

    protected void parseMetaProperties(final int offset, String content) {
        Constants.splitLine(content, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String line, boolean isDelimiter) {
                if(isDelimiter) {
                    emit(getDefaultToken(), offset + startOffset, line.length());
                    return;
                }
                Matcher matcher = metaProperty.matcher(line);
                if(matcher.find()) {
                    emit(metaPropertyToken, offset + startOffset, matcher.end());
                    emit(getDefaultToken(), offset + startOffset + matcher.end(), line.length()-matcher.end());
                }
                else
                    emit(getDefaultToken(), offset + startOffset, line.length());
            }
        });
    }

}
