package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartVisitor;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.New;
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
public class StepScannerStyled implements ITokenScanner {
    
    private IToken defaultToken;
    private IToken keywordToken;
    private IToken parameterToken;
    private IToken parameterValueToken;
    //
    private List<Fragment> fragments;
    private int cursor = 0;
    //
    private IDocument document;
    private Region range;
    private StepLocator.Provider locatorProvider;
    private Token exampleTableSepToken;
    private Token exampleTableCellToken;

    public StepScannerStyled(StepLocator.Provider locatorProvider, TextAttributeProvider textAttributeProvider) {
        initializeTokens(textAttributeProvider);
        this.locatorProvider = locatorProvider;
    }
    
    private void initializeTokens(TextAttributeProvider textAttributeProvider) {
        TextAttribute textAttribute = textAttributeProvider.get(StoryTextAttributes.Step);
        defaultToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepKeyword);
        keywordToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepParameter);
        parameterToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepParameterValue);
        parameterValueToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepExampleTableSep);
        exampleTableSepToken = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepExampleTableCell);
        exampleTableCellToken = new Token(textAttribute);
    }

    /*
     * Returns the length of the last token read by this scanner.
     * 
     * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
     */
    @Override
    public int getTokenLength() {
        return fragments.get(cursor).getLength();
    }
    
    /*
     * Returns the offset of the last token read by this scanner.
     * 
     * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
     */
    @Override
    public int getTokenOffset() {
        return fragments.get(cursor).getOffset();
    }
    
    /*
     * Returns the next token in the document.
     * 
     * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
     */
    @Override
    public IToken nextToken() {
        if(cursor==-1) {
            evaluateFragments();
        }
        cursor++;
        if(cursor<fragments.size())
            return fragments.get(cursor).getToken();
        return Token.EOF;
    }
    
    private void evaluateFragments() {
        StoryPartVisitor visitor = new StoryPartVisitor() {
            @Override
            public void visit(StoryPart part) {
                if(part.intersects(range.getOffset(), range.getLength()) && part.isStepPart())
                    emitPart(0, part); //part are given in the absolute position
            }
        };
        StoryPartDocumentUtils.traverseStoryParts(document, visitor);
        
        if(DEBUG) {
            System.out.println(builder);
            builder.setLength(0);
        }
    }
    
    private static boolean DEBUG = false;
    private StringBuilder builder = new StringBuilder();
    private void logln(String string) {
        if(DEBUG)
            builder.append(string).append('\n');
    }
    
    private void emitPart(int offset_delta, StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        if(keyword!=null && keyword.isStep()) {
            parseStep(part.getContent(), offset_delta+part.getOffset());
        }
        else {
            emit(defaultToken, offset_delta+part.getOffset(), part.getLength());
        }
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
                    emit(defaultToken, offset, content.length());
                }
                offset += content.length();
            }
        }
        else {
            logln("parseStep(" + stepContent + ") step found without variable");
            emit(defaultToken, offset, afterKeyword.length());
            offset += afterKeyword.length();
        }
        
        // insert if trailings whitespace have been removed
        int expectedOffset = initialOffset+stepContent.length();
        if(offset < expectedOffset) {
            emit(defaultToken, offset, expectedOffset-offset);
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
                emit(defaultToken, offset, length);
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
                
                IToken token = defaultToken;
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
            IToken token = defaultToken;
            if(inVariable) {
                token = parameterToken;
            }
            
            // emit remaining
            emit(token, offset + tokenStart, i-tokenStart);
        }
    }
    
    private void emit(IToken token, int offset, int length) {
        logln("emit(" + token.getData() + ", offset: " + offset + ", length: " + length + ")");
        
        // can we merge previous one?
        if(!fragments.isEmpty()) {
            Fragment previous = getLastFragment();
            
            // check no hole
            int requiredOffset = previous.offset+previous.length;
            if(offset != requiredOffset) {
                logln("emit() hole completion, offset: " +  offset + ", length: " + length + "; previous offset: " + previous.offset + ", length: " + previous.length);
                emit(defaultToken, requiredOffset, requiredOffset-offset);
                previous = getLastFragment();
            }
            
            if(previous.token==token) {
                previous.length += length;
                logln("emit() token merged, offset: " +  previous.offset + ", length: " + previous.length);
                return;
            }
        }
        Fragment fragment = new Fragment(token, offset, length);
        logln("emit() >>> added, offset: " +  offset + ", length: " + length);
        fragments.add(fragment);
    }

    private Fragment getLastFragment() {
        return fragments.get(fragments.size()-1);
    }

    /*
     * Configures the scanner by providing access to the document range that should be scanned.
     * 
     * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
     */
    @Override
    public void setRange(IDocument document, int offset, int length) {
        logln("setRange(offset: " +  offset + ", length: " + length);

        fragments = New.arrayList();
        cursor = -1;
        this.document = document;
        this.range = new Region(offset, length);
    }
     
    public class Fragment {
        private IToken token;
        private int offset, length;
        private Fragment(IToken token, int offset, int length) {
            super();
            this.token = token;
            this.offset = offset;
            this.length = length;
        }
        @Override
        public String toString() {
            try {
                return token.getData() + ", offset: " + offset + ", length: " + length + ", c>>" + document.get(offset, length)+"<<";
            } catch (BadLocationException e) {
                return token.getData() + ", offset: " + offset + ", length: " + length + ", c>>" + "//BadLocationException//" +"<<";
            }
        }
        public int getOffset() {
            return offset;
        }
        public int getLength() {
            return length;
        }
        public IToken getToken() {
            return token;
        }
    }
}
