package org.technbolts.jbehave.eclipse.editors.story.scanner;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;

public class AllInOneScanner extends AbstractStoryPartBasedScanner {
    
    public static boolean allInOne = true;
    
    private ExampleTableScanner exampleTableScanner;
    private MiscScanner miscScanner;
    private NarrativeScanner narrativeScanner;
    private ScenarioScanner scenarioScanner;
    private StepScannerStyled stepScannerStyled;

    private Region realRange;

    public AllInOneScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
        exampleTableScanner = new ExampleTableScanner(jbehaveProject, textAttributeProvider);
        miscScanner = new MiscScanner(jbehaveProject, textAttributeProvider);
        narrativeScanner = new NarrativeScanner(jbehaveProject, textAttributeProvider);
        scenarioScanner = new ScenarioScanner(jbehaveProject, textAttributeProvider);
        stepScannerStyled = new StepScannerStyled(jbehaveProject, textAttributeProvider);
    }
    
    @Override
    public void setRange(IDocument document, int offset, int length) {
        realRange = new Region(offset, length);
        super.setRange(document, 0, document.getLength());
    }
    
    @Override
    protected void evaluateFragments() {
        super.evaluateFragments();
        Iterator<Fragment> iterator = getFragments().iterator();
        while(iterator.hasNext()) {
            Fragment fragment = iterator.next();
            if(!fragment.intersects(realRange)) {
                iterator.remove();
            }
        }
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        TextAttribute textAttribute = textAttributeProvider.get(TextStyle.DEFAULT);
        setDefaultToken(new Token(textAttribute));
    }

    @Override
    protected boolean isPartAccepted(StoryPart part) {
        return true;
    }
    
    @Override
    protected void emitPart(StoryPart part) {
        JBKeyword keyword = part.getPreferredKeyword();
        switch(keyword) {
            case Given:
            case When:
            case Then:
            case And:
                emitPart(stepScannerStyled, part);
                break;
            case ExamplesTable:
            case ExamplesTableHeaderSeparator:
            case ExamplesTableIgnorableSeparator:
            case ExamplesTableRow:
            case ExamplesTableValueSeparator:
                emitPart(exampleTableScanner, part);
                break;
            case Narrative:
            case AsA:
            case InOrderTo:
            case IWantTo:
                emitPart(narrativeScanner, part);
                break;
            case GivenStories:
            case Meta:
            case MetaProperty:
                emitPart(miscScanner, part);
                break;
            case Scenario:
                emitPart(scenarioScanner, part);
                break;
            case Ignorable:
            default:
                emitCommentAware(getDefaultToken(), part.getOffset(), part.getContent());
                break;
        }
    }

    private void emitPart(AbstractStoryPartBasedScanner scanner, StoryPart part) {
        scanner.setRange(document, 0, document.getLength());
        scanner.emitPart(part);
        addFragments(scanner.getFragments());
    }
}
