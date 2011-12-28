package org.technbolts.eclipse.jbehave;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.junit.Before;
import org.junit.Test;
import org.technbolts.jbehave.eclipse.editors.story.StoryPartitionScanner;

public class PartitionScannerTest {
private String storyAsText;
    
    @Before
    public void setUp () throws IOException {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx01.story"));
    }

    @Test
    public void usecase_ex1() throws Exception {
        IDocument document= new Document(storyAsText);
        
        IToken defaultToken= new Token("defaultToken");
        StoryPartitionScanner scanner = new StoryPartitionScanner ();
        scanner.setRange(document, 0, document.getLength());
        
        checkNextToken(scanner, document, "Narrative", 0, 172);
        checkNextToken(scanner, document, "Scenario", 172, 57);
        checkNextToken(scanner, document, "Step", 229, 208);
        System.out.println(scanner.nextToken().isEOF());
    }
    
    private void checkNextToken(IPartitionTokenScanner scanner, IDocument document, Object jk, int offset, int length) throws BadLocationException {
        IToken token = scanner.nextToken();
        assertEquals(jk, token.getData());
        //System.out.print(jk + " > ");
        //dumpState(scanner, document);
        assertEquals(offset, scanner.getTokenOffset());
        assertEquals(length, scanner.getTokenLength());
        
    }
    
    private static void dumpState(RuleBasedScanner scanner, IDocument doc) throws BadLocationException {
        int tokenOffset = scanner.getTokenOffset();
        int tokenLength = scanner.getTokenLength();
        System.out.print(tokenOffset + " ~> " + tokenLength);
        System.out.println(" > Ò" + doc.get(tokenOffset, tokenLength) + "Ó");
    }

}
