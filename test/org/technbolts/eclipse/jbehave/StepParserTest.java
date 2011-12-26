package org.technbolts.eclipse.jbehave;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.jbehave.core.steps.StepType;
import org.junit.Before;
import org.junit.Test;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.editors.story.StepScannerStyled;
import org.technbolts.jbehave.eclipse.editors.story.StoryTextAttributes;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.eclipse.util.StepLocator.Provider;

public class StepParserTest {
    
    private String storyAsText;
    private TextAttribute defaultAttr;
    private TextAttribute keywordAttr;
    private TextAttribute paramAttr;
    private TextAttribute paramValueAttr;
    private StepLocator locator;
    private TextAttributeProvider textAttributeProvider;
    private Provider locatorProvider;
    //

    private int offset;
    
    @Before
    public void setUp () throws IOException {
        defaultAttr = mock(TextAttribute.class);
        keywordAttr = mock(TextAttribute.class);
        paramAttr = mock(TextAttribute.class);
        paramValueAttr = mock(TextAttribute.class);
        when(defaultAttr.toString()).thenReturn("mock-default");
        when(keywordAttr.toString()).thenReturn("mock-keyword");
        when(paramAttr.toString()).thenReturn("mock-parameter");
        when(paramValueAttr.toString()).thenReturn("mock-parameter-value");
        
        textAttributeProvider = mock(TextAttributeProvider.class);
        when(textAttributeProvider.get(StoryTextAttributes.Step)).thenReturn(defaultAttr);
        when(textAttributeProvider.get(StoryTextAttributes.StepKeyword)).thenReturn(keywordAttr);
        when(textAttributeProvider.get(StoryTextAttributes.StepParameter)).thenReturn(paramAttr);
        when(textAttributeProvider.get(StoryTextAttributes.StepParameterValue)).thenReturn(paramValueAttr);
        
        locator = mock(StepLocator.class);
        locatorProvider = mock(StepLocator.Provider.class);
        when(locatorProvider.getStepLocator()).thenReturn(locator);
        
        this.offset = 0;
    }

    private static PotentialStep givenStep(String content) {
        return new PotentialStep(null, null, StepType.GIVEN, content);
    }
    
    private static PotentialStep whenStep(String content) {
        return new PotentialStep(null, null, StepType.WHEN, content);
    }
    
    private static PotentialStep thenStep(String content) {
        return new PotentialStep(null, null, StepType.THEN, content);
    }
    
    @Test
    public void usecase_ex1() throws Exception {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx02.story"));
        
        when(locator.findFirstStep("a user named \"Bob\"")).thenReturn(new PotentialStep(null, null, StepType.GIVEN, "a user named \"$name\""));
        when(locator.findFirstStep("'Bob' clicks on the 'login' button")).thenReturn(new PotentialStep(null, null, StepType.WHEN, "'$who' clicks on the '$button' button"));
        when(locator.findFirstStep("the 'password' field becomes 'red'")).thenReturn(new PotentialStep(null, null, StepType.THEN, "the '$field' field becomes '$color'"));
        
        IDocument document= new Document(storyAsText);
        
        StepScannerStyled scanner= new StepScannerStyled(locatorProvider, textAttributeProvider);
        scanner.setRange(document, 0, document.getLength());

        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);

        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        
        assertEquals(document.getLength(), offset);
    }
    
    @Test
    public void usecase_ex2() throws Exception {
        PotentialStep user = givenStep("a user named $username");
        PotentialStep credits = whenStep("user credits is $amount dollars");
        PotentialStep clicks = whenStep("user clicks on $button button");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx03.story"));
        when(locator.findFirstStep("a user named $username")).thenReturn(user);
        when(locator.findFirstStep("user clicks on $button button")).thenReturn(clicks);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        when(locator.findFirstStep("a user named username")).thenReturn(user);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        
        IDocument document= new Document(storyAsText);
        
        offset = 1;
        
        StepScannerStyled scanner= new StepScannerStyled(locatorProvider, textAttributeProvider);
        scanner.setRange(document, offset, 161);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertEquals(document.getLength(), offset);
    }
    
    @Test
    public void usecase_ex3 () throws Exception {
        PotentialStep user = givenStep("a user named $username");
        PotentialStep credits = whenStep("user credits is $amount dollars");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx03.story"));
        when(locator.findFirstStep("a user named $username")).thenReturn(user);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        when(locator.findFirstStep("a user named username")).thenReturn(user);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        
        IDocument document= new Document(storyAsText);
        
        offset = 1;
        
        StepScannerStyled scanner= new StepScannerStyled(locatorProvider, textAttributeProvider);
        scanner.setRange(document, offset, 161);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramValueAttr);
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertEquals(document.getLength(), offset);
    }
    
    @Test
    public void usecase_ex4() throws Exception {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/domain/i_can_login_using_parameters_table.story"));
        
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScannerStyled scanner= new StepScannerStyled(locatorProvider, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength());
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertEquals(document.getLength(), offset);
    }
    
    @Test
    public void usecase_ex5() throws Exception {
        PotentialStep seeHomePage = thenStep("agent see the application home page");
        when(locator.findFirstStep("agent see the application home page")).thenReturn(seeHomePage);
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/domain/i_can_login_using_parameters_table.story"));
        IDocument document= new Document(storyAsText);
        offset = 477;
        
        StepScannerStyled scanner= new StepScannerStyled(locatorProvider, textAttributeProvider);
        scanner.setRange(document, offset, 179);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertEquals(477+179, offset);
    }

    private void consumeRemaining(IDocument document, StepScannerStyled scanner) throws BadLocationException {
        IToken token = scanner.nextToken();
        while(!token.isEOF()) {
            offset += scanner.getTokenLength();
            dumpState(scanner, document);
            token = scanner.nextToken();
        }
    }
    
    private void checkToken(StepScannerStyled scanner, IDocument document, Object jk) throws BadLocationException {
        System.out.print(jk + " > ");
        IToken token = scanner.nextToken();
        dumpState(scanner, document);
        assertEquals(jk, token.getData());
        assertEquals(offset, scanner.getTokenOffset());
        offset += scanner.getTokenLength();
    }
    
    private static void dumpState(StepScannerStyled scanner, IDocument doc) throws BadLocationException {
        int tokenOffset = scanner.getTokenOffset();
        int tokenLength = scanner.getTokenLength();
        System.out.print(tokenOffset + " ~> " + tokenLength);
        System.out.println(" > Ò" + doc.get(tokenOffset, tokenLength) + "Ó");
    }
    
    
}
