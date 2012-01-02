package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.eclipse.util.StepLocator;
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
    
    private TextAttributeProvider textAttributeProvider;
    //
    private IToken keywordToken;
    private IToken parameterToken;
    private IToken parameterValueToken;
    private Token exampleTableSepToken;
    private Token exampleTableCellToken;
    //
    private StepLocator.Provider locatorProvider;

    public StepScannerStyled(StepLocator.Provider locatorProvider, TextAttributeProvider textAttributeProvider) {
        this.textAttributeProvider = textAttributeProvider;
        initialize();
        textAttributeProvider.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                initialize();
            }
        });
        this.locatorProvider = locatorProvider;
    }
    
    private void initialize() {
        System.out.println("StepScannerStyled.initialize()****\n****\n****");
        
        TextAttribute textAttribute = textAttributeProvider.get(TextStyle.STEP_DEFAULT);
        setDefaultToken(new Token(textAttribute));
        
        textAttribute = textAttributeProvider.get(TextStyle.STEP_KEYWORD);
        keywordToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(TextStyle.STEP_PARAMETER);
        parameterToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(TextStyle.STEP_PARAMETER_VALUE);
        parameterValueToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR);
        exampleTableSepToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(TextStyle.STEP_EXAMPLE_TABLE_CELL);
        exampleTableCellToken = new Token(textAttribute);
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
        logln("parseStep(" + stepContent + ", offset: " + initialOffset + ", stepLine.length: " + stepContent.length());
        int offset = initialOffset;
        int stepSep = stepContent.indexOf(' ');
         
        emit(keywordToken, offset, stepSep+1);
        offset += stepSep+1;
        
        // remove any trailing newlines, and keep track to insert 
        // corresponding token in place
        String afterKeyword = stepContent.substring(stepSep+1);
        String stepSentence = Strings.removeTrailingNewlines(afterKeyword);
        
        PotentialStep potentialStep = locatorProvider.getStepLocator().findFirstStep(stepSentence);
        
        if(potentialStep==null) {
            logln("parseStep() no step found");
            emitVariables(afterKeyword, offset);
            offset += afterKeyword.length();
        }
        else if(potentialStep.hasVariable()) {

            ParametrizedString pString = potentialStep.getParametrizedString();
            WeightChain chain = pString.calculateWeightChain(stepSentence);
            List<String> chainTokens = chain.tokenize();
            
            logln("parseStep() step found with variable " + chainTokens.size() + " tokens in chain");

            for(int i=0;i<chainTokens.size();i++) {
                org.technbolts.util.ParametrizedString.Token pToken = pString.getToken(i);
                String content = chainTokens.get(i);
                
                if(pToken.isIdentifier) {
                    
                    logln("token is an identifier content: >>" + content.replace("\n", "\\n") + "<<");
                    
                    if(content.startsWith("$")) {
                        emit(parameterToken, offset, content.length());
                    }
                    else {
                        String trimmed = content.trim();
                        
                        logln("trimmed: >>" + trimmed.replace("\n", "\\n") + "<<");

                        if(trimmed.startsWith("|")) {
                            emitTable(offset, content);
                        }
                        else {
                            emit(parameterValueToken, offset, content.length());
                        }
                    }
                }
                else {
                    emit(getDefaultToken(), offset, content.length());
                }
                offset += content.length();
            }
        }
        else {
            logln("parseStep(" + stepContent + ") step found without variable");
            emit(getDefaultToken(), offset, afterKeyword.length());
            offset += afterKeyword.length();
        }
        
        // insert if trailings whitespace have been removed
        int expectedOffset = initialOffset+stepContent.length();
        if(offset < expectedOffset) {
            emit(getDefaultToken(), offset, expectedOffset-offset);
        }
    }

    private void emitTable(int offset, String content) {
        StringTokenizer tokenizer = new StringTokenizer(content, "|", true);
        int remaining = tokenizer.countTokens();
        boolean isFirst = true;
        while(tokenizer.hasMoreTokens()) {
            boolean isLast = (remaining==1);
            String tok = tokenizer.nextToken();
            int length = tok.length();
            
            logln("StepScannerStyled.emitTable(token: >>" +tok.replace("\n", "\\n") + "<<");
            
            if(tok.equals("|")) {
                emit(exampleTableSepToken, offset, length);
            }
            else if(isLast || isFirst) {
                emit(getDefaultToken(), offset, length);
            }
            else {
                emit(exampleTableCellToken, offset, length);
            }
            
            offset += length;
            remaining--;
            isFirst = false;
        }
    }

    private void emitVariables(String content, int offset) {
        logln("emitVariables(offset: " + offset + ", content.length: " + content.length() + " >>" + content + "<<");
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
                emit(token, offset + tokenStart, i-tokenStart);
                inVariable = true;
                tokenStart = i;
            }
            else if(inVariable) {
                if(Character.isJavaIdentifierPart(c))
                    continue;
                // emit previous
                emit(parameterToken, offset + tokenStart, i-tokenStart);
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
            emit(token, offset + tokenStart, i-tokenStart);
        }
    }
    
}
