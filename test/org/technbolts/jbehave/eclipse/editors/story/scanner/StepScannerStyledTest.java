package org.technbolts.jbehave.eclipse.editors.story.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.jbehave.core.steps.StepType;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.util.Visitor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StepScannerStyledTest {
    private static final String STEP1 = "an account named '$name' with the following properties:$properties";
    public static final String NL = "\n";
    
    public static final String GIVEN1 = "Given an account named 'Travis' with the following properties:" + NL +
            "|key|value|" + NL +
            "!-- Some comment" + NL + 
            "|Login|Travis|" + NL +
            "|Password|p4cm4n|" + NL;
    
    private TextAttributeProvider textAttributeProvider;
    private StepScannerStyled scanner;
    private Document document;
    private StepLocator stepLocator;
    private PotentialStep potentialStep;
    private LocalizedStepSupport localizedStepSupport;
    private JBehaveProject jbehaveProject;

    @SuppressWarnings("rawtypes")
    @BeforeMethod
    public void prepare () throws JavaModelException {
        stepLocator = mock(StepLocator.class);
        textAttributeProvider = mock(TextAttributeProvider.class);
        localizedStepSupport = new LocalizedStepSupport();
        localizedStepSupport.setStoryLocale(Locale.ENGLISH);
        jbehaveProject = mock(JBehaveProject.class);
        when(jbehaveProject.getLocalizedStepSupport()).thenReturn(localizedStepSupport);
        when(jbehaveProject.getStepLocator()).thenReturn(new StepLocator(jbehaveProject));
        
        doAnswer(new Answer() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Visitor<PotentialStep, ?> visitor = (Visitor<PotentialStep, ?>)invocation.getArguments()[0];
                visitor.visit(potentialStep);
                return null;
            }
        }).when(jbehaveProject).traverseSteps(Mockito.<Visitor<PotentialStep, ?>>any());
        
        scanner = new StepScannerStyled(jbehaveProject, textAttributeProvider) {
            @Override
            protected Token newToken(String styleId) {
                return new Token(styleId);
            }
        };
        document = new Document(GIVEN1);
        IMethod method = null;
        IAnnotation annotation = null;
        potentialStep = new PotentialStep(localizedStepSupport, "$", method, annotation, StepType.GIVEN, STEP1, 0);
    }
    
    @Test
    public void useCase_ex1() {
        when(stepLocator.findFirstStep(Mockito.anyString())).thenAnswer(new Answer<PotentialStep>() {
            @Override
            public PotentialStep answer(InvocationOnMock invocation) throws Throwable {
                System.out.println("StepScannerStyledTest.useCase_ex1(" + invocation + ")");
                return potentialStep;
            }
        });
        
        String[] expected = { //
                "step_keyword ~ offset: 0, length: 6", //
                "step_default ~ offset: 6, length: 18", //
                "step_parameter_value ~ offset: 24, length: 6", //
                "step_default ~ offset: 30, length: 33", //
                "step_example_table_separator ~ offset: 63, length: 1", //
                "step_example_table_cell ~ offset: 64, length: 3", //
                "step_example_table_separator ~ offset: 67, length: 1", //
                "step_example_table_cell ~ offset: 68, length: 5", //
                "step_example_table_separator ~ offset: 73, length: 1", //
                "step_default ~ offset: 74, length: 1", //
                "comment ~ offset: 75, length: 17", //
                "step_example_table_separator ~ offset: 92, length: 1", //
                "step_example_table_cell ~ offset: 93, length: 5", //
                "step_example_table_separator ~ offset: 98, length: 1", //
                "step_example_table_cell ~ offset: 99, length: 6", //
                "step_example_table_separator ~ offset: 105, length: 1", //
                "step_default ~ offset: 106, length: 1", //
                "step_example_table_separator ~ offset: 107, length: 1", //
                "step_example_table_cell ~ offset: 108, length: 8", //
                "step_example_table_separator ~ offset: 116, length: 1", //
                "step_example_table_cell ~ offset: 117, length: 6", //
                "step_example_table_separator ~ offset: 123, length: 1", //
                "step_default ~ offset: 124, length: 1" //
        };
        
        int index = 0;
        scanner.setRange(document, 0, document.getLength());
        IToken token = scanner.nextToken();
        while(!token.isEOF()) {
            String actual = (token.getData() + " ~ offset: " + scanner.getTokenOffset() + ", length: " + scanner.getTokenLength());
            assertThat(actual, equalTo(expected[index++]));
            token = scanner.nextToken();
        }
        assertThat(index, equalTo(expected.length));
    }
}
