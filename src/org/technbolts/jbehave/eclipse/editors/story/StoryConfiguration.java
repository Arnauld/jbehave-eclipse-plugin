package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.editors.story.completion.StepContentAssistant;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.support.JBPartition;

public class StoryConfiguration extends SourceViewerConfiguration {

    private ITokenScanner stepScanner;
    private ITokenScanner defaultScanner;
    private ITokenScanner commentScanner;
    private ITokenScanner scenarioScanner;
    private ITokenScanner narrativeScanner;
    private ITokenScanner miscScanner;
    private ITokenScanner exampleTableScanner;
    private TextAttributeProvider textAttributeProvider;
    private StoryEditor storyEditor;
    private PresentationReconciler reconciler;

    public StoryConfiguration(StoryEditor storyEditor, TextAttributeProvider textAttributeProvider) {
        this.storyEditor = storyEditor;
        this.textAttributeProvider = textAttributeProvider;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
     */
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        List<String> types = JBPartition.names();
        types.add(IDocument.DEFAULT_CONTENT_TYPE);
        return types.toArray(new String[types.size()]);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        return new StepContentAssistant();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IHyperlinkDetector[] getHyperlinkDetectors(final ISourceViewer sourceViewer) {
        return new IHyperlinkDetector[] { new StepHyperLinkDetector() };
    }

    private ITokenScanner getDefaultScanner() {
        if (defaultScanner == null) {
            defaultScanner = createScanner(TextStyle.DEFAULT);
        }
        return defaultScanner;
    }

    protected ITokenScanner getStepScanner() {
        if (stepScanner == null) {
            stepScanner = new StepScannerStyled(new StepLocator.Provider() {
                @Override
                public StepLocator getStepLocator() {
                    return StepLocator.getStepLocator(storyEditor.getProject());
                }
            }, textAttributeProvider);
        }
        return stepScanner;
    }
    
    protected ITokenScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = createScanner(TextStyle.COMMENT);
        }
        return commentScanner;
    }
    
    protected ITokenScanner getScenarioScanner() {
        if (scenarioScanner == null) {
            scenarioScanner = new ScenarioScanner(textAttributeProvider);
        }
        return scenarioScanner;
    }
    
    protected ITokenScanner getNarrativeScanner() {
        if (narrativeScanner == null) {
            narrativeScanner = new NarrativeScanner(textAttributeProvider);
        }
        return narrativeScanner;
    }
    
    protected ITokenScanner getMiscScanner() {
        if (miscScanner == null) {
            miscScanner = new MiscScanner(textAttributeProvider);
        }
        return miscScanner;
    }
    
    protected ITokenScanner getExampleTableScanner() {
        if (exampleTableScanner == null) {
            exampleTableScanner = new ExampleTableScanner(textAttributeProvider);
        }
        return exampleTableScanner;
    }

    private ITokenScanner createScanner(String attributeKey) {
        return new SingleTokenScanner(textAttributeProvider, attributeKey);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getDefaultScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getStepScanner());
        reconciler.setDamager(dr, JBPartition.Step.name());
        reconciler.setRepairer(dr, JBPartition.Step.name());

        dr = new DefaultDamagerRepairer(getCommentScanner());
        reconciler.setDamager(dr, JBPartition.Comment.name());
        reconciler.setRepairer(dr, JBPartition.Comment.name());

        dr = new DefaultDamagerRepairer(getScenarioScanner());
        reconciler.setDamager(dr, JBPartition.Scenario.name());
        reconciler.setRepairer(dr, JBPartition.Scenario.name());
        
        dr = new DefaultDamagerRepairer(getNarrativeScanner());
        reconciler.setDamager(dr, JBPartition.Narrative.name());
        reconciler.setRepairer(dr, JBPartition.Narrative.name());
        
        dr = new DefaultDamagerRepairer(getExampleTableScanner());
        reconciler.setDamager(dr, JBPartition.ExampleTable.name());
        reconciler.setRepairer(dr, JBPartition.ExampleTable.name());
        
        dr = new DefaultDamagerRepairer(getMiscScanner());
        reconciler.setDamager(dr, JBPartition.Misc.name());
        reconciler.setRepairer(dr, JBPartition.Misc.name());
        
        return reconciler;
    }
}