package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.editors.story.completion.StepContentAssistProcessor;
import org.technbolts.jbehave.support.JBPartition;

public class StoryConfiguration extends SourceViewerConfiguration {

    private RuleBasedScanner stepScanner;
    private RuleBasedScanner defaultScanner;
    private RuleBasedScanner commentScanner;
    private RuleBasedScanner scenarioScanner;
    private RuleBasedScanner narrativeScanner;
    private TextAttributeProvider textAttributeProvider;

    public StoryConfiguration(TextAttributeProvider textAttributeProvider) {
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

        IContentAssistProcessor stepProcessor = new StepContentAssistProcessor(); 
        ContentAssistant assistant = new ContentAssistant() {
            @Override
            public IContentAssistProcessor getContentAssistProcessor(String contentType) {
                IContentAssistProcessor processor = super.getContentAssistProcessor(contentType);
                Activator.logInfo("StoryConfiguration.getContentAssistant(...).new ContentAssistant() {...}.getContentAssistProcessor(" + contentType + ") :: " + processor);
                return processor;
            }
        };
        assistant.setContentAssistProcessor(stepProcessor, JBPartition.Step.name());
        assistant.setContentAssistProcessor(stepProcessor, (String)TokenConstants.IGNORED.getData());
        assistant.setContentAssistProcessor(stepProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(500);
        assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        return assistant;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IHyperlinkDetector[] getHyperlinkDetectors(final ISourceViewer sourceViewer) {
        return new IHyperlinkDetector[] { new StepHyperLinkDetector() };
    }

    private RuleBasedScanner getDefaultScanner() {
        if (defaultScanner == null) {
            defaultScanner = createScanner(textAttributeProvider.get(StoryTextAttributes.Default));
        }
        return defaultScanner;
    }

    protected RuleBasedScanner getStepScanner() {
        if (stepScanner == null) {
            stepScanner = new StepScanner(textAttributeProvider);
        }
        return stepScanner;
    }
    
    protected RuleBasedScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = createScanner(textAttributeProvider.get(StoryTextAttributes.Comment));
        }
        return commentScanner;
    }
    
    protected RuleBasedScanner getScenarioScanner() {
        if (scenarioScanner == null) {
            scenarioScanner = new ScenarioScanner(textAttributeProvider);
        }
        return scenarioScanner;
    }
    
    protected RuleBasedScanner getNarrativeScanner() {
        if (narrativeScanner == null) {
            narrativeScanner = new NarrativeScanner(textAttributeProvider);
        }
        return narrativeScanner;
    }

    private RuleBasedScanner createScanner(TextAttribute textAttribute) {
        RuleBasedScanner scanner = new RuleBasedScanner();
        scanner.setDefaultReturnToken(new Token(textAttribute));
        return scanner;
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

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
        
        return reconciler;
    }

}