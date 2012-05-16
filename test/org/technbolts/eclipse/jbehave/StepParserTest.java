package org.technbolts.eclipse.jbehave;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.jbehave.core.steps.StepType;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.editors.story.scanner.StepScannerStyled;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StepParserTest {
    private Logger log = LoggerFactory.getLogger(StepParserTest.class);

    private String storyAsText;
    private TextAttribute defaultAttr;
    private TextAttribute keywordAttr;
    private TextAttribute paramAttr;
    private TextAttribute paramValueAttr;
    private TextAttribute exampleTableSep;
    private StepLocator locator;
    private TextAttributeProvider textAttributeProvider;
    private JBehaveProject jbehaveProject;
    //
    private static LocalizedStepSupport localizedSupport = createLocalizedStepSupport();

    private int offset;
    private TextAttribute exampleTableCell;
    
    @BeforeMethod
    public void setUp () throws IOException {
        defaultAttr = mock(TextAttribute.class);
        keywordAttr = mock(TextAttribute.class);
        paramAttr = mock(TextAttribute.class);
        paramValueAttr = mock(TextAttribute.class);
        exampleTableSep = mock(TextAttribute.class);
        exampleTableCell = mock(TextAttribute.class);
        
        when(defaultAttr.toString()).thenReturn("mock-default");
        when(keywordAttr.toString()).thenReturn("mock-keyword");
        when(paramAttr.toString()).thenReturn("mock-parameter");
        when(paramValueAttr.toString()).thenReturn("mock-parameter-value");
        when(exampleTableSep.toString()).thenReturn("mock-table-sep");
        when(exampleTableCell.toString()).thenReturn("mock-table-cell");
        
        textAttributeProvider = mock(TextAttributeProvider.class);
        when(textAttributeProvider.get(TextStyle.STEP_DEFAULT)).thenReturn(defaultAttr);
        when(textAttributeProvider.get(TextStyle.STEP_KEYWORD)).thenReturn(keywordAttr);
        when(textAttributeProvider.get(TextStyle.STEP_PARAMETER)).thenReturn(paramAttr);
        when(textAttributeProvider.get(TextStyle.STEP_PARAMETER_VALUE)).thenReturn(paramValueAttr);
        when(textAttributeProvider.get(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR)).thenReturn(exampleTableSep);
        when(textAttributeProvider.get(TextStyle.STEP_EXAMPLE_TABLE_CELL)).thenReturn(exampleTableCell);
        
        locator = mock(StepLocator.class);
        
        this.offset = 0;
        
        jbehaveProject = mock(JBehaveProject.class);
        when(jbehaveProject.getLocalizedStepSupport()).thenReturn(localizedSupport);
        when(jbehaveProject.getStepLocator()).thenReturn(locator);
    }

    private static LocalizedStepSupport createLocalizedStepSupport() {
        LocalizedStepSupport localizedSupport = new LocalizedStepSupport();
        localizedSupport.setStoryLocale(Locale.ENGLISH);
        return localizedSupport;
    }

    private static PotentialStep givenStep(String content) {
        return new PotentialStep(localizedSupport, "$", null, null, StepType.GIVEN, content, 0);
    }
    
    private static PotentialStep whenStep(String content) {
        return new PotentialStep(localizedSupport, "$", null, null, StepType.WHEN, content, 0);
    }
    
    private static PotentialStep thenStep(String content) {
        return new PotentialStep(localizedSupport, "$", null, null, StepType.THEN, content, 0);
    }
    
    @Test
    public void usecase_ex1() throws Exception {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx02.story"));
        
        when(locator.findFirstStep("a user named \"Bob\"")).thenReturn(givenStep("a user named \"$name\""));
        when(locator.findFirstStep("'Bob' clicks on the 'login' button")).thenReturn(whenStep("'$who' clicks on the '$button' button"));
        when(locator.findFirstStep("the 'password' field becomes 'red'")).thenReturn(thenStep("the '$field' field becomes '$color'"));
        
        IDocument document= new Document(storyAsText);
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
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
        
        assertThat(offset, equalTo(document.getLength()));
    }
    @Test
    public void usecase_ex2_light() throws Exception {
        PotentialStep user = givenStep("a user named $username");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx03-light.story"));
        when(locator.findFirstStep("a user named $username")).thenReturn(user);
        
        IDocument document= new Document(storyAsText);
        
        offset = 1;
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength()-offset);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        checkToken(scanner, document, paramAttr);
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
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
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength()-offset);
        
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
        
        assertThat(offset, equalTo(document.getLength()));
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
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength()-offset);
        
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
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    public void usecase_ex04_exampleTable () throws Exception {
        final PotentialStep user = givenStep("a new account named 'networkAgent' with the following properties (properties not set will be completed) $exampleTable");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx04.story"));
        when(locator.findFirstStep(Mockito.anyString())).thenAnswer(new Answer<PotentialStep>() {
            @Override
            public PotentialStep answer(InvocationOnMock invocation) throws Throwable {
                String searched = (String) invocation.getArguments()[0];
                if(searched.startsWith("a new account named 'networkAgent' with the following properties"))
                    return user;
                else
                    return null;
            }
        });
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength());
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        
        checkToken(scanner, document, defaultAttr); // NL

        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        
        checkToken(scanner, document, defaultAttr); // NL

        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    public void usecase_ex05_exampleTable () throws Exception {
        final PotentialStep user = givenStep("a new account named 'networkAgent' with the following properties (properties not set will be completed) $exampleTable");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx06-exampletable.story"));
        when(locator.findFirstStep(Mockito.anyString())).thenAnswer(new Answer<PotentialStep>() {
            @Override
            public PotentialStep answer(InvocationOnMock invocation) throws Throwable {
                String searched = (String) invocation.getArguments()[0];
                if(searched.startsWith("a new account named 'networkAgent' with the following properties"))
                    return user;
                else
                    return null;
            }
        });
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength());
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);

        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        
        checkToken(scanner, document, defaultAttr); // NL
        
        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSep);
        
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test(enabled=false)
    public void usecase_ex4() throws Exception {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/domain/i_can_login_using_parameters_table.story"));
        
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
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
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test(enabled=false)
    public void usecase_ex5() throws Exception {
        PotentialStep seeHomePage = thenStep("agent see the application home page");
        when(locator.findFirstStep("agent see the application home page")).thenReturn(seeHomePage);
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/domain/i_can_login_using_parameters_table.story"));
        IDocument document= new Document(storyAsText);
        offset = 477;
        
        StepScannerStyled scanner= new StepScannerStyled(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, 179);
        
        checkToken(scanner, document, keywordAttr);
        checkToken(scanner, document, defaultAttr);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(477+179));
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
        log.debug(jk + " > ");
        IToken token = scanner.nextToken();
        dumpState(scanner, document);

        assertThat(token.getData(), equalTo(jk));
        assertThat(offset, equalTo(scanner.getTokenOffset()));
        offset += scanner.getTokenLength();
    }
    
    private void dumpState(StepScannerStyled scanner, IDocument doc) throws BadLocationException {
        int tokenOffset = scanner.getTokenOffset();
        int tokenLength = scanner.getTokenLength();
        log.debug(tokenOffset + " ~> " + tokenLength + " >>" + doc.get(tokenOffset, tokenLength) + "<<");
    }
    
    
}
