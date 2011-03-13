package org.technbolts.eclipse.jbehave;

import static org.junit.Assert.assertEquals;
import static org.technbolts.util.Strings.removeTrailingNewlines;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.junit.Before;
import org.junit.Test;
import org.technbolts.jbehave.eclipse.editors.story.StoryParserRule;
import org.technbolts.jbehave.support.JBKeyword;

public class ParserTest {
    
    private String storyAsText;
    
    @Before
    public void setUp () throws IOException {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx01.story"));
    }

    @Test
    public void usecase_ex1() throws Exception {
        IDocument document= new Document(storyAsText);
        
        IToken defaultToken= new Token("defaultToken");
        RuleBasedScanner scanner= new RuleBasedScanner();
        scanner.setRules(new IRule[] { new StoryParserRule(false) });
        scanner.setRange(document, 0, document.getLength());
        scanner.setDefaultReturnToken(defaultToken);
        
        checkToken(scanner, document, JBKeyword.Narrative);
        checkToken(scanner, document, JBKeyword.InOrderTo);
        checkToken(scanner, document, JBKeyword.AsA);
        checkToken(scanner, document, JBKeyword.IWantTo);
        checkToken(scanner, document, JBKeyword.Scenario);
        checkToken(scanner, document, JBKeyword.Given);
        checkToken(scanner, document, JBKeyword.When);
        checkToken(scanner, document, JBKeyword.Then);
        checkToken(scanner, document, JBKeyword.When);
        checkToken(scanner, document, JBKeyword.Then);
    }

    
    private void checkToken(RuleBasedScanner scanner, IDocument document, JBKeyword jk) throws BadLocationException {
        assertEquals(jk, scanner.nextToken().getData());
        System.out.print(jk + " > ");
        dumpState(scanner, document);
    }
    
    private static void dumpState(RuleBasedScanner scanner, IDocument doc) throws BadLocationException {
        int tokenOffset = scanner.getTokenOffset();
        int tokenLength = scanner.getTokenLength();
        System.out.print(tokenOffset + " ~> " + tokenLength);
        System.out.println(" > Ò" + removeTrailingNewlines(doc.get(tokenOffset, tokenLength)) + "Ó");
    }
    
}
