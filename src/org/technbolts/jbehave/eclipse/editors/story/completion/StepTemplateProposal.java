package org.technbolts.jbehave.eclipse.editors.story.completion;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.util.WeightedCandidateStep;

public class StepTemplateProposal extends TemplateProposal implements 
        ICompletionProposalExtension4, ICompletionProposalExtension5,
        ICompletionProposalExtension6, StepCompletionProposalMixin.Trait {
    
    private final LocalizedStepSupport jbehaveProject;
    private final String complete;
    private final String label;
    private final WeightedCandidateStep weightedCandidateStep;

    public StepTemplateProposal(
            LocalizedStepSupport jbehaveProject, //
            Template template, TemplateContext context, IRegion region, 
            String complete, String label, WeightedCandidateStep weightedCandidateStep) {
        super(template, context, region, null, 0);
        this.jbehaveProject = jbehaveProject;
        this.complete = complete;
        this.label = label;
        this.weightedCandidateStep = weightedCandidateStep;
    }
    
    @Override
    public LocalizedStepSupport getJBehaveProject() {
        return jbehaveProject;
    }
    
    @Override
    public boolean isAutoInsertable() {
        return false;
    }

    @Override
    public String getDisplayString() {
        // by default it is <name> - <description>
        return getStyledDisplayString().getString();
    }

    /* (non-Javadoc)
     * @see org.technbolts.jbehave.eclipse.editors.story.completion.StepCompletionProposalMixin.Trait#getWeightedCandidateStep()
     */
    @Override
    public WeightedCandidateStep getWeightedCandidateStep() {
        return weightedCandidateStep;
    }

    /* (non-Javadoc)
     * @see org.technbolts.jbehave.eclipse.editors.story.completion.StepCompletionProposalMixin.Trait#getComplete()
     */
    @Override
    public String getComplete() {
        return complete;
    }
    
    @Override
    public String getLabel() {
        return label;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension6#getStyledDisplayString()
     */
    @Override
    public StyledString getStyledDisplayString() {
        return StepCompletionProposalMixin.createStyledString(this);
    }
    
    @Override
    public Image getImage() {
        return StepCompletionProposalMixin.getImage(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension5#getAdditionalProposalInfo(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return StepCompletionProposalMixin.getAdditionalHTML(this);
    }
    
}
