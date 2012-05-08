package org.technbolts.jbehave.eclipse.editors.story.scanner;

import static org.technbolts.util.Objects.o;

import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.parser.Constants;
import org.technbolts.jbehave.parser.ContentWithIgnorableEmitter;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.ParametrizedString;
import org.technbolts.util.ParametrizedString.WeightChain;
import org.technbolts.util.Strings;

/**
 * 
 * 
 * {@link ITokenScanner} javadoc: <br/>
 * 
 * <p>
 * A token scanner scans a range of a document and reports about the token it finds. 
 * <b>A scanner has state</b>. When asked, the scanner returns the offset and the length 
 * of the last found token.
 * </p>
 */
public class StepScannerStyled extends AbstractStoryPartBasedScanner {
    
    private Logger log = LoggerFactory.getLogger(StepScannerStyled.class);

    //
    private IToken keywordToken;
    private IToken parameterToken;
    private IToken parameterValueToken;
    //

    public StepScannerStyled(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        
        setDefaultToken(newToken(TextStyle.STEP_DEFAULT));
        keywordToken = newToken(TextStyle.STEP_KEYWORD);
        parameterToken = newToken(TextStyle.STEP_PARAMETER);
        parameterValueToken = newToken(TextStyle.STEP_PARAMETER_VALUE);
        exampleTableSepToken = newToken(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR);
        exampleTableCellToken = newToken(TextStyle.STEP_EXAMPLE_TABLE_CELL);
    }
    
    @Override
    protected boolean isPartAccepted(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(keyword!=null && keyword.isStep()) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void emitPart(StoryPart part) {
        parseStep(part.getContent(), part.getOffset());
    }
    
    private void parseStep(String stepContent, final int initialOffset) {
        log.debug("Parsing step, offset: {}, length: {}, content: <{}>", 
                o(initialOffset, stepContent.length(), f(stepContent)));
        int offset = initialOffset;
        int stepSep = LineParser.stepSentenceIndex(getLocalizedStepSupport(), stepContent);
         
        emit(keywordToken, offset, stepSep);
        offset += stepSep;
        
        // remove any trailing newlines, and keep track to insert 
        // corresponding token in place
        String rawAfterKeyword = stepContent.substring(stepSep);
        ContentWithIgnorableEmitter emitter = new ContentWithIgnorableEmitter(
                Constants.commentLineMatcher, rawAfterKeyword);
        
        String cleanedAfterKeyword = emitter.contentWithoutIgnorables();
        String cleanedStepSentence = Strings.removeTrailingNewlines(cleanedAfterKeyword);
        
        PotentialStep potentialStep = getStepLocator().findFirstStep(cleanedStepSentence);
        if(potentialStep==null) {
            log.debug("No step found");
            emitVariables(emitter, cleanedAfterKeyword, offset);
            offset += rawAfterKeyword.length();
        }
        else if(potentialStep.hasVariable()) {

            ParametrizedString pString = potentialStep.getParametrizedString();
            WeightChain chain = pString.calculateWeightChain(cleanedStepSentence);
            List<String> chainTokens = chain.tokenize();
            
            log.debug("Step found with variable {} tokens in chain", chainTokens.size());

            for(int i=0;i<chainTokens.size();i++) {
                org.technbolts.util.ParametrizedString.Token pToken = pString.getToken(i);
                String content = chainTokens.get(i);
                
                log.debug("Token content - length: {}, content: <{}>",
                        o(content.length() , f(content)));
                
                if(pToken.isIdentifier) {
                    
                    log.debug("Token is an identifier <{}>", f(content));
                    
                    if(content.startsWith("$")) {
                        emit(emitter, parameterToken, offset, content.length());
                    }
                    else {
                        if(Constants.containsExampleTable(content)) {
                            emitTable(emitter, getDefaultToken(), offset, content);
                        }
                        else {
                            emit(emitter, parameterValueToken, offset, content.length());
                        }
                    }
                }
                else {
                    emit(emitter, getDefaultToken(), offset, content.length());
                }
                offset += content.length();
            }
        }
        else {
            log.debug("Parsing step <{}> step found without variable", f(stepContent));
            emit(emitter, getDefaultToken(), offset, cleanedAfterKeyword.length());
            offset += rawAfterKeyword.length();
        }
        
        // insert if trailings whitespace have been removed
        int expectedOffset = initialOffset+(stepSep + cleanedAfterKeyword.length());
        if(offset < expectedOffset) {
            log.debug("Remaings, offset: {}, length: {}", o(offset, expectedOffset-offset));
            emit(emitter, getDefaultToken(), offset, expectedOffset-offset);
        }
    }

    private StepLocator getStepLocator() {
        StepLocator stepLocator = jbehaveProject.getStepLocator();
        if(stepLocator==null)
            throw new IllegalStateException("No state locator available from project");
        return stepLocator;
    }

    private void emitVariables(ContentWithIgnorableEmitter emitter, String content, int offset) {
        log.debug("Emitting variables (offset: {}, length: {}, <{}>",
                 o(offset, content.length(), f(content)));
        int tokenStart = 0;
        boolean escaped = false;
        boolean inVariable = false;
        int i=0;
        for(; i<(content.length()); i++) {
            char c = content.charAt(i);
            if(c=='$') {
                if(escaped)
                    continue;
                
                IToken token = getDefaultToken();
                if(inVariable) {
                    token = parameterToken;
                }
                
                // emit previous
                emit(emitter, token, offset + tokenStart, i-tokenStart);
                inVariable = true;
                tokenStart = i;
            }
            else if(inVariable) {
                if(Character.isJavaIdentifierPart(c))
                    continue;
                // emit previous
                emit(emitter, parameterToken, offset + tokenStart, i-tokenStart);
                inVariable = false;
                tokenStart = i;
            }
        }
        
        // remaining?
        if(i>tokenStart) {
            IToken token = getDefaultToken();
            if(inVariable) {
                token = parameterToken;
            }
            
            // emit remaining
            emit(emitter, token, offset + tokenStart, i-tokenStart);
        }
    }
    
}
