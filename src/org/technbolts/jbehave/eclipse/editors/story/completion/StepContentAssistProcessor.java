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
import org.jbehave.core.configuration.Keywords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.eclipse.util.TemplateUtils;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.JBehaveProjectRegistry;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils;
import org.technbolts.jbehave.eclipse.util.WeightedCandidateStep;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.Lists;
import org.technbolts.util.New;
import org.technbolts.util.Strings;

public class StepContentAssistProcessor implements IContentAssistProcessor {
    
    private Logger logger = LoggerFactory.getLogger(StepContentAssistProcessor.class);
    
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, final int offset) {
        try {
            IProject project = EditorUtils.findProject(viewer);
            JBehaveProject jbehaveProject = JBehaveProjectRegistry.get().getOrCreateProject(project);
            LocalizedStepSupport localizedStepSupport = jbehaveProject.getLocalizedStepSupport();

            IDocument document = viewer.getDocument();
            int lineNo = document.getLineOfOffset(offset);
            int lineOffset = document.getLineOffset(lineNo);
            boolean isWithinLine = (lineOffset < offset);
            
            String partitionText = "";
            int index = offset;
            String lineStart = "";

            boolean relyOnPartition = false;
            if(offset>0) {
                // retrieve region before 'cause we are probably in the next one
                ITypedRegion region = document.getPartition(offset-1);
                int partitionOffset = region.getOffset();
                int partitionLength = region.getLength();
                index = (offset - 1) - partitionOffset;
                partitionText = document.get(partitionOffset, partitionLength);
                if(isWithinLine) {
                    lineStart = Strings.getSubLineUntilOffset(partitionText, index+1);
                }
                
                // keep partition infos for logging, but search line content by an other way
                if(!relyOnPartition) {
                    logger.debug("Autocompletion retrieving content lineOffset: " + lineOffset + ", offset: " + offset);
                    lineStart = document.get(lineOffset, offset-lineOffset);
                }
            }
            
            logger.debug("Autocompletion offset: {} partion text: <{}>", offset, partitionText);
            logger.debug("Autocompletion line start: <{}>", lineStart);

            if(StringUtils.isEmpty(lineStart)) {
                return createKeywordCompletionProposals(offset, 0, viewer);
            }
            else if(LineParser.isTheStartIgnoringCaseOfStep(localizedStepSupport, lineStart) //
                    && !LineParser.isStepType(localizedStepSupport, lineStart)) {
                return createKeywordCompletionProposals(lineOffset, lineStart.length(), viewer);
            }
            
            // TODO add support for multi-line step
            final String stepStart = lineStart;
            
            String stepStartUsedForSearch = stepStart;
            // special case: one must find the right type of step
            boolean isAndCase = LineParser.isStepAndType(localizedStepSupport, lineStart); 
            if(isAndCase) {
                StoryPart part = new StoryPartDocumentUtils(localizedStepSupport).findStoryPartAtOffset(document, offset).get();
                JBKeyword kw = part.getPreferredKeyword();
                if(kw == JBKeyword.And) {
                    logger.debug("Autocompletion unable to disambiguate 'And' case: previous story part is probably not a step");
                    return null;
                }
                int indexOf = localizedStepSupport.lAnd(false).length();
                stepStartUsedForSearch = kw.asString(localizedStepSupport.getLocalizedKeywords()) + lineStart.substring(indexOf);
            }
            
            logger.debug("Autocompletion step start used for search: <{}>", stepStartUsedForSearch);
            
            Iterable<WeightedCandidateStep> candidateIter = jbehaveProject.getStepLocator().findCandidatesStartingWith(stepStartUsedForSearch);
            List<WeightedCandidateStep> candidates = Lists.toList(candidateIter);
            Collections.sort(candidates);
            logger.debug("Autocompletion found #{}", candidates.size());
            
            String stepEntry = LineParser.extractStepSentence(localizedStepSupport, stepStart);
            boolean hasStartOfStep = !StringUtils.isBlank(stepEntry);
            
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
                if(hasStartOfStep) {
                    complete = pStep.potentialStep.getParametrizedString().complete(stepEntry);
                    templateContext = contextComplete;
                    replacementRegion = regionComplete;
                    displayString = lineStart + complete;
                }
                else {
                    complete = pStep.potentialStep.fullStep();
                    if(isAndCase) {
                        complete = localizedStepSupport.lAnd(false) + " " + pStep.potentialStep.stepPattern;
                    }
                    templateContext = contextFullLine;
                    replacementRegion = regionComplete;
                    displayString = complete;
                }
                complete += "\n";

                int cursor = complete.indexOf('$');
                if(cursor<0) {
                    cursor = complete.length();
                    ICompletionProposal proposal = null;
                    int mode = 2;
                    switch(mode) {
                        case 1: 
                            proposal = new CompletionProposal(
                                complete,
                                replacementRegion.getOffset(),
                                replacementRegion.getLength(),
                                cursor,
                                null,
                                displayString,
                                null,
                                displayString);
                            break;
                        default:
                            proposal = new StepCompletionProposal(localizedStepSupport, replacementRegion, complete, displayString, pStep);
                    }
                    proposals.add(proposal);
                }
                else {
                    String templateText = TemplateUtils.templatizeVariables(complete);
                    Template template = new Template(
                            lineStart, 
                            displayString, 
                            StoryContextType.STORY_CONTEXT_TYPE_ID, templateText, false);
                    proposals.add(new StepTemplateProposal(localizedStepSupport,
                            template,
                            templateContext, replacementRegion, complete, displayString, pStep));
                }
            }
            
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

    private ICompletionProposal[] createKeywordCompletionProposals(int offset, int length, ITextViewer viewer) {
        List<ICompletionProposal> proposals = New.arrayList();

        JBKeyword[] keywords = new JBKeyword[] {
                JBKeyword.Given,
                JBKeyword.And,
                JBKeyword.When,
                JBKeyword.Then,
                JBKeyword.Scenario,
                JBKeyword.GivenStories };
        Keywords jkeywords = new Keywords();
        for(JBKeyword keyword : keywords) {
            String kw = keyword.asString(jkeywords);
            proposals.add(new CompletionProposal(kw, offset, length, kw.length()));
        };
        
        StoryTemplateCompletionProcessor t = new StoryTemplateCompletionProcessor();
        proposals.addAll(Arrays.asList(t.computeCompletionProposals(viewer, offset)));
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
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
