package org.technbolts.jbehave.eclipse.editors.story;

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
public class StepScannerStyledBak implements ITokenScanner {
    
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

    public StepScannerStyledBak(StepLocator.Provider locatorProvider, TextAttributeProvider textAttributeProvider) {
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
        StoryParser parser = new StoryParser(false);
        BidirectionalReader reader = Rules.createReader(createCharacterScanner());
        
        while(!reader.eof()) {
            int startPosition = reader.getPosition();
            JBKeyword keyword = parser.nextKeyword(reader);
            int endPosition = reader.getPosition();
            StringBuilder builder = reader.backToPosition(startPosition).readUntil(endPosition);
            
            System.out.println("StepScannerStyled.evaluateFragments(#" + builder.length() + ">>"+ builder + "<<)");
            
            if(reader.eof())
                endPosition--;
            
            if(builder.length()==0)
                continue;
            
            if(keyword==null || !keyword.isStep()) {
                emit(defaultToken, startPosition, endPosition-startPosition);
            }
            else {
                parseStep(builder, startPosition, endPosition-startPosition);
            }
        }
        
        // remaining?
        if(reader.getPosition() < (range.getOffset() + range.getLength())) {
            System.out.println("StepScannerStyled.evaluateFragments():: emiting remaining");
            emit(defaultToken, reader.getPosition() , range.getOffset() + range.getLength());
        }
        
        System.out.println("StepScannerStyled.evaluateFragments() == Computed fragment:");
        for(int i=0;i<fragments.size();i++) {
            Fragment f = fragments.get(i);            
            System.out.println("## " + f);
        }
    }

    private void parseStep(StringBuilder builder, final int startPosition, int length) {
        int position = startPosition;
        String stepLine = builder.toString();
        
        int stepSep = Strings.indexOf(builder, ' ');
         
        emit(keywordToken, position, stepSep+1);
        position += stepSep+1;
        
        // remove any trailing newlines, and keep track to insert 
        // corresponding token in place
        String trailingCleaned = Strings.removeTrailingNewlines(stepLine);
        String stepSentence = trailingCleaned.substring(stepSep+1);
        
        PotentialStep potentialStep = locatorProvider.getStepLocator().findFirstStep(stepSentence);
        
        if(potentialStep==null) {
            emitVariables(stepSentence, position);
            position += stepSentence.length();
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
                emit(token, position, content.length());
                position += content.length();
            }
        }
        else {
            emit(defaultToken, position, length);
            position += length;
        }
        
        // remaining
        if(position<(startPosition+length)) {
            emit(defaultToken, position, startPosition+length-position);
        }
    }

    private void emitVariables(String stepLine, int startPosition) {
        int tokenStart = 0;
        boolean escaped = false;
        boolean inVariable = false;
        int i=0;
        for(; i<(stepLine.length()); i++) {
            char c = stepLine.charAt(i);
            if(c=='$') {
                if(escaped)
                    continue;
                
                IToken token = defaultToken;
                if(inVariable) {
                    token = parameterToken;
                }
                
                // emit previous
                emit(token, startPosition + tokenStart, startPosition + i-1);
                inVariable = true;
                tokenStart = i;
            }
            else if(inVariable) {
                if(Character.isJavaIdentifierPart(c))
                    continue;
                // emit previous
                emit(parameterToken, startPosition + tokenStart, startPosition + i-1);
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
            emit(token, startPosition + tokenStart, startPosition + i-1);
        }
    }

    private void emit(IToken token, int startPosition, int length) {
        int start  = range.getOffset()+startPosition;
        
        System.out.println("StepScannerStyled.emit(local: " + startPosition+" (absolute: " + start + ", range: " + range.getOffset() + "::" + range.getLength() + "), length: " + length);
        
        // can we merge previous one?
        if(!fragments.isEmpty()) {
            Fragment previous = getLastFragment();
            
            System.out.println("StepScannerStyled.emit():: check no hole :: " + start + " =? " + previous);
            
            // check no hole
            if(start != (previous.offset+previous.length)) {
                int pos = previous.offset+previous.length;
                emit(defaultToken, pos, start-pos);
                previous = getLastFragment();
            }
            
            if(previous.token==token) {
                int before = previous.length;
                previous.length += length;
                System.out.println("StepScannerStyled.emit(**updated** " + token.getData() + " start: " + previous.offset + " length from " + before + " to " + previous.length);
                return;
            }
        }
        System.out.println("StepScannerStyled.emit(" + token.getData() + " start: " + start + " length: " + length);
        Fragment fragment = new Fragment(token, start, length);
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
