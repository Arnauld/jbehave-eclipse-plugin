package org.technbolts.eclipse.jbehave;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.editors.story.scanner.StoryPartitionScanner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PartitionScannerTest {

    private String storyAsText;
    private LocalizedStepSupport localizedSupport;
    private JBehaveProject jbehaveProject;
    
    @BeforeMethod
    public void setUp () throws IOException {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx01.story"));
        jbehaveProject = mock(JBehaveProject.class);
        localizedSupport = new LocalizedStepSupport();
        localizedSupport.setStoryLocale(Locale.ENGLISH);
    }

    @Test
    public void usecase_ex1() throws Exception {
        when(jbehaveProject.getLocalizedStepSupport()).thenReturn(localizedSupport);
        IDocument document= new Document(storyAsText);
        
        StoryPartitionScanner scanner = new StoryPartitionScanner (jbehaveProject);
        scanner.setRange(document, 0, document.getLength());
        
        checkNextToken(scanner, document, "Narrative", 0, 172);
        checkNextToken(scanner, document, "Scenario", 172, 57);
        checkNextToken(scanner, document, "Step", 229, 208);
        assertThat(scanner.nextToken().isEOF(), is(true));
    }
    
    private void checkNextToken(IPartitionTokenScanner scanner, IDocument document, Object jk, int offset, int length) throws BadLocationException {
        IToken token = scanner.nextToken();
        assertThat(token.getData(), equalTo(jk));
        //System.out.print(jk + " > ");
        //dumpState(scanner, document);
        assertThat(scanner.getTokenOffset(), equalTo(offset));
        assertThat(scanner.getTokenLength(), equalTo(length));
        
    }
    
}
