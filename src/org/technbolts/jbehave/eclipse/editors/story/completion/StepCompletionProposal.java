package org.technbolts.jbehave.eclipse.editors.story.completion;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.util.WeightedCandidateStep;

public class StepCompletionProposal implements ICompletionProposal,
    ICompletionProposalExtension4,    
    ICompletionProposalExtension5, ICompletionProposalExtension6, StepCompletionProposalMixin.Trait {

    private final LocalizedStepSupport jbehaveProject;
    private final Region replacementRegion;
    private final String complete;
    private final String label;
    private final WeightedCandidateStep weightedStep;
    private StyledString styledString;
    private IContextInformation contextInformation;

    public StepCompletionProposal(LocalizedStepSupport jbehaveProject, Region replacementRegion, String complete, String label, WeightedCandidateStep pStep) {
        super();
        this.jbehaveProject = jbehaveProject;
        this.replacementRegion = replacementRegion;
        this.complete = complete;
        this.label = label;
        this.weightedStep = pStep;
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
    public WeightedCandidateStep getWeightedCandidateStep() {
        return weightedStep;
    }
    
    @Override
    public String getComplete() {
        return complete;
    }
    
    @Override
    public String getLabel() {
        return label;
    }
    
    @Override
    public void apply(IDocument document) {
        try {
            document.replace(replacementRegion.getOffset(), replacementRegion.getLength(), complete);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAdditionalProposalInfo() {
        return StepCompletionProposalMixin.getAdditionalHTML(this);
    }

    @Override
    public IContextInformation getContextInformation() {
        return contextInformation;
    }

    @Override
    public String getDisplayString() {
        return getStyledDisplayString().getString();
    }

    @Override
    public Image getImage() {
        return StepCompletionProposalMixin.getImage(this);
    }

    @Override
    public Point getSelection(IDocument document) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StyledString getStyledDisplayString() {
        if(styledString==null)
            styledString = StepCompletionProposalMixin.createStyledString(this);
        return styledString;
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return StepCompletionProposalMixin.getAdditionalHTML(this);
    }

}
