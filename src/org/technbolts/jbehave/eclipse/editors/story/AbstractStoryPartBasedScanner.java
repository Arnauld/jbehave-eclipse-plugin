package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartVisitor;
import org.technbolts.util.New;

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
public abstract class AbstractStoryPartBasedScanner implements ITokenScanner {
    
    private IToken defaultToken;
    //
    private List<Fragment> fragments;
    private int cursor = 0;
    //
    private IDocument document;
    private Region range;

    public AbstractStoryPartBasedScanner() {
    }
    
    public void setDefaultToken(IToken defaultToken) {
        if(defaultToken==null)
            throw new IllegalArgumentException();
        this.defaultToken = defaultToken;
    }
    
    public IToken getDefaultToken() {
        return defaultToken;
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
    
    protected void evaluateFragments() {
        StoryPartVisitor visitor = new StoryPartVisitor() {
            @Override
            public void visit(StoryPart part) {
                if(part.intersects(range.getOffset(), range.getLength()) && isPartAccepted(part))
                    emitPart(part); //part are given in the absolute position
            }
        };
        StoryPartDocumentUtils.traverseStoryParts(document, visitor);
        
        if(DEBUG) {
            System.out.println(builder);
            builder.setLength(0);
        }
    }
    
    protected abstract boolean isPartAccepted(StoryPart part);

    private static boolean DEBUG = false;
    private StringBuilder builder = new StringBuilder();
    protected void logln(String string) {
        if(DEBUG)
            builder.append(string).append('\n');
    }
    
    protected abstract void emitPart(StoryPart part);

    protected void emit(IToken token, int offset, int length) {
        logln("emit(" + token.getData() + ", offset: " + offset + ", length: " + length + ")");
        
        // can we merge previous one?
        if(!fragments.isEmpty()) {
            Fragment previous = getLastFragment();
            
            // check no hole
            int requiredOffset = previous.offset+previous.length;
            if(offset != requiredOffset) {
                logln("emit() hole completion, offset: " +  offset + ", length: " + length + "; previous offset: " + previous.offset + ", length: " + previous.length);
                emit(getDefaultToken(), requiredOffset, requiredOffset-offset);
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
