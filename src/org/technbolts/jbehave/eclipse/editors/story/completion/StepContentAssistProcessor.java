package org.technbolts.jbehave.eclipse.editors.story.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.eclipse.util.TemplateUtils;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.eclipse.util.StepLocator.WeightedCandidateStep;
import org.technbolts.util.Lists;
import org.technbolts.util.New;
import org.technbolts.util.Strings;

public class StepContentAssistProcessor implements IContentAssistProcessor {
    
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, final int offset) {
        try {
            IDocument document = viewer.getDocument();
            int lineNo = document.getLineOfOffset(offset);
            int lineOffset = document.getLineOffset(lineNo);
            boolean isWithinLine = (lineOffset < offset);
            
            // retrieve region before 'cause we are probably in the next one
            ITypedRegion region = document.getPartition(offset-1);
            int partitionOffset = region.getOffset();
            int partitionLength = region.getLength();
            int index = (offset - 1) - partitionOffset;
            
            String partitionText = document.get(partitionOffset, partitionLength);
            String lineStart = "";
            if(isWithinLine) {
                lineStart = Strings.getSubLineUntilOffset(partitionText, index+1);
            }

            Activator.logInfo("\n" +
                            "....Offset........: " + offset + "\n" + 
                            "lineOffset........: " + lineOffset + "\n" + 
                            "Partition text....: " + partitionText.substring(0, index) + "<*>" + partitionText.substring(index) + "\n" +
                            "partitionOffset...: " + partitionOffset + "\n" +
                            "Index.............: " + index + "\n" +
                            "lineStart.........:>" + lineStart + "<" + "\n" + 
                            "isTheStartOfAStep.: " + LineParser.isTheStartIgnoringCaseOfStep(lineStart)
                    );
            if(StringUtils.isEmpty(lineStart)) {
                return createKeywordCompletionProposals(offset, 0);
            }
            else if(LineParser.isTheStartIgnoringCaseOfStep(lineStart) && !LineParser.isStepType(lineStart)) {
                return createKeywordCompletionProposals(lineOffset, lineStart.length());
            }
            
            // TODO add support for multi-line step
            final String stepStart = lineStart;
            
            IProject project = EditorUtils.findProject(viewer);
            Iterable<WeightedCandidateStep> candidateIter = StepLocator.getStepLocator(project).findCandidatesStartingWith(stepStart);
            List<WeightedCandidateStep> candidates = Lists.toList(candidateIter);
            Collections.sort(candidates);
            
            String stepEntry = LineParser.extractStepSentence(stepStart);
            boolean isEmpty  = StringUtils.isBlank(stepEntry);
            
            final StoryTemplateCompletionProcessor t = new StoryTemplateCompletionProcessor();
            

            Region regionFullLine = new Region(lineOffset, lineStart.length());
            Region regionComplete = new Region(offset, 0);
            
            TemplateContext contextFullLine = createTemplateContext(document, regionFullLine);
            TemplateContext contextComplete = createTemplateContext(document, regionComplete);

            List<ICompletionProposal> proposals = New.arrayList();
            for(int i=0;i<candidates.size();i++) {
                WeightedCandidateStep pStep = candidates.get(i);
                
                String displayString;
                String complete;
                TemplateContext templateContext;
                Region replacementRegion;
                if(!isEmpty) {
                    complete = pStep.potentialStep.getParametrizedString().complete(stepEntry);
                    templateContext = contextComplete;
                    replacementRegion = regionComplete;
                    displayString = lineStart + complete;
                }
                else {
                    complete = pStep.potentialStep.fullStep();
                    templateContext = contextFullLine;
                    replacementRegion = regionComplete;
                    displayString = complete;
                }

                Activator.logInfo(">>> " + pStep.weight + " // >" + complete + "< // >" + stepStart + "< // " + pStep.potentialStep);
                
                int cursor = complete.indexOf('$');
                if(cursor<0) {
                    cursor = complete.length();
                    proposals.add(new CompletionProposal(
                            complete,
                            replacementRegion.getOffset(),
                            replacementRegion.getLength(),
                            cursor,
                            null,
                            displayString,
                            null,
                            null));
                }
                else {
                    String templateText = TemplateUtils.templatizeVariables(complete);
                    Template template = new Template(
                            lineStart, 
                            displayString, 
                            StoryContextType.STORY_CONTEXT_TYPE_ID, templateText, false);
                    proposals.add(new StoryTemplateProposal(template,
                            templateContext, replacementRegion, null, 0));
                }
            }
            
            proposals.addAll(Arrays.asList(t.computeCompletionProposals(viewer, offset)));
            return proposals.toArray(new ICompletionProposal[proposals.size()]);

        } catch (BadLocationException e) {
            e.printStackTrace();
        } 
        return null;
    }

    private DocumentTemplateContext createTemplateContext(IDocument document, Region region) {
        TemplateContextType contextType = StoryContextType.getTemplateContextType();
        return new DocumentTemplateContext(contextType, document, region.getOffset(), region.getLength());
    }

    private ICompletionProposal[] createKeywordCompletionProposals(int offset, int length) {
        String[] keywords = new String[] {"Given ", "When ", "Then ", "And "};
        ICompletionProposal[] result = new ICompletionProposal[keywords.length];
        for(int i=0;i<keywords.length;i++) {
            String kw = keywords[i];
            result[i] = new CompletionProposal(kw, offset, length, kw.length());
        };
        return result;
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
        return "Oops failure within content assist";
    }
}
