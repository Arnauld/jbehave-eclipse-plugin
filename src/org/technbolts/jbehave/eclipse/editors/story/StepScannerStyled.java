package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.util.StringEnhancer.enhanceString;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.rule.Rules;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.StoryParser;
import org.technbolts.util.BidirectionalReader;
import org.technbolts.util.New;
import org.technbolts.util.ParametrizedString;
import org.technbolts.util.ParametrizedString.WeightChain;
import org.technbolts.util.StringEnhancer;
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
        ICharacterScanner scanner = createCharacterScanner();
        int read;
        int offset = range.getOffset();
        Buffer buffer = new Buffer();
        while((read=scanner.read())!=ICharacterScanner.EOF) {
            buffer.push(offset, (char)read);
            offset++;
        }
        //remaining
        buffer.emitData();
    }
    
    private class Buffer {
        private StringBuilder data = new StringBuilder();
        private boolean inNewLines = false;
        private int startOffset;
        private boolean accept(char c) {
            if(isNewline(c)) {
                if(!inNewLines && data.length()>0)
                    return false;
                return true;
            }
            else if(inNewLines) {
                return false;
            }
            else {
                return true;
            }
        }
        private boolean isNewline(char c) {
            return c=='\r' || c=='\n';
        }
        public void push(int offset, char c) {
            System.out.print("StepScannerStyled.Buffer.push(" + (char) c + ", offset: " + offset +") data before #" + data.length());
            if(!accept(c)) {
                System.out.println(" NOT ACCEPTED");
                emitData();
                reset(offset);
            }
            else if(data.length()==0) {
                System.out.println(" ACCEPTED");
                reset(offset);
            }
            else {
                System.out.println(" ACCEPTED");
            }
            append(c);
        }
        private void append(char c) {
            inNewLines |= isNewline(c);
            data.append(c);
        }
        private void emitData() {
            if(data.length()==0) {
                return;
            }
            String string = data.toString();
            if(enhanceString(string).startsIgnoringCaseWithOneOf("given ", "when ", "then ")) {
                parseStep(string, startOffset);
            }
            else {
                emit(defaultToken, startOffset, data.length());
            }
        }
        private void reset(int offset) {
            inNewLines = false;
            startOffset = offset;
            data.setLength(0);
        }
    }

    private void parseStep(String stepLine, final int initialOffset) {
        System.out.println("StepScannerStyled.parseStep(" + stepLine + "), initialOffset: " + initialOffset);
        int offset = initialOffset;
        int stepSep = stepLine.indexOf(' ');
         
        emit(keywordToken, offset, stepSep+1);
        offset += stepSep+1;
        
        // remove any trailing newlines, and keep track to insert 
        // corresponding token in place
        String afterKeyword = stepLine.substring(stepSep+1);
        String stepSentence = Strings.removeTrailingNewlines(afterKeyword);
        
        PotentialStep potentialStep = locatorProvider.getStepLocator().findFirstStep(stepSentence);
        
        if(potentialStep==null) {
            emitVariables(afterKeyword, offset);
            offset += afterKeyword.length();
        }
        else if(potentialStep.hasVariable()) {
            ParametrizedString pString = potentialStep.getParametrizedString();
            
            WeightChain chain = pString.calculateWeightChain(stepSentence);
            List<String> chainTokens = chain.tokenize();
            
            for(int i=0;i<chainTokens.size();i++) {
                org.technbolts.util.ParametrizedString.Token pToken = pString.getToken(i);
                String content = chainTokens.get(i);
                
                IToken token = defaultToken;
                if(pToken.isIdentifier) {
                    if(content.startsWith("$"))
                        token = parameterToken;
                    else
                        token = parameterValueToken;
                }
                emit(token, offset, content.length());
                offset += content.length();
            }
        }
        else {
            emit(defaultToken, offset, stepLine.length());
            offset += stepLine.length();
        }
        
        // insert if trailings whitespace have been removed
        int expectedOffset = initialOffset+stepLine.length();
        if(offset < expectedOffset) {
            emit(defaultToken, offset, expectedOffset-offset);
        }
    }

    private void emitVariables(String content, int offset) {
        System.out.println("StepScannerStyled.emitVariables(" + content + ", " + offset + ")");
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
            emit(token, offset + tokenStart, offset + i-1);
        }
    }

    private void emit(IToken token, int offset, int length) {
        
        System.out.println("StepScannerStyled.emit(offset: " + offset+" (range: " + range.getOffset() + "::" + range.getLength() + "), length: " + length);
        
        // can we merge previous one?
        if(!fragments.isEmpty()) {
            Fragment previous = getLastFragment();
            
            System.out.println("StepScannerStyled.emit():: check no hole :: " + offset + " =? " + previous);
            
            // check no hole
            int requiredOffset = previous.offset+previous.length;
            if(offset != requiredOffset) {
                emit(defaultToken, requiredOffset, offset-requiredOffset);
                previous = getLastFragment();
            }
            
            if(previous.token==token) {
                int before = previous.length;
                previous.length += length;
                System.out.println("StepScannerStyled.emit(**updated** " + token.getData() + " start: " + previous.offset + " length from " + before + " to " + previous.length);
                return;
            }
        }
        System.out.println("StepScannerStyled.emit(" + token.getData() + " start: " + offset + " length: " + length);
        Fragment fragment = new Fragment(token, offset, length);
        fragments.add(fragment);
    }

    private Fragment getLastFragment() {
        return fragments.get(fragments.size()-1);
    }

    private ICharacterScanner createCharacterScanner() {
        return Rules.createScanner(document, range.getOffset(), range.getLength());
    }

    /*
     * Configures the scanner by providing access to the document range that should be scanned.
     * 
     * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
     */
    @Override
    public void setRange(IDocument document, int offset, int length) {
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
