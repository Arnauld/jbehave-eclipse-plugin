package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.custom.StyledText;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.StepLocator;

public class StepContentAssistProcessor implements IContentAssistProcessor {

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        try {
            IDocument document = viewer.getDocument();
            ITypedRegion region = document.getPartition(offset);
            int partitionOffset = region.getOffset();
            int partitionLength = region.getLength();
            int index = offset - partitionOffset;

            String partitionText = document.get(partitionOffset, partitionLength);
            System.out.println("Partition text: " + partitionText.substring(0, index) + "<*>" + partitionText.substring(index));
            System.out.println("Index: " + index);
            
            StyledText widget = viewer.getTextWidget();
            final int lineNo = widget.getLineAtOffset(region.getOffset());
            final int lineOffset = widget.getOffsetAtLine(lineNo);
            
            // TODO add support for multi-line step
            final String stepStart = partitionText.substring(0, index);
            
            IProject project = EditorUtils.findProject(viewer);
            List<PotentialStep> candidates = StepLocator.getStepLocator(project).findCandidatesStartingWith(stepStart);
            ICompletionProposal[] result = new ICompletionProposal[candidates.size()];
            for(int i=0;i<candidates.size();i++) {
                PotentialStep potentialStep = candidates.get(i);
                
                int cursor = potentialStep.stepPattern.indexOf("$");
                if(cursor<0)
                    cursor = potentialStep.stepPattern.length();
                cursor += potentialStep.typeWord ().length()+1;
                
                result[i] = new CompletionProposal(
                        potentialStep.fullStep(),
                        lineOffset,
                        partitionText.length()-1,
                        cursor);
            }
            
            return result;

        } catch (BadLocationException e) {
            e.printStackTrace();
        } 
        return null;
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
