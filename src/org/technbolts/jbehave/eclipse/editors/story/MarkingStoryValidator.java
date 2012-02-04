package org.technbolts.jbehave.eclipse.editors.story;

import static fj.data.List.iterableList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.util.MarkData;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils;
import org.technbolts.jbehave.parser.Constants;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.New;
import org.technbolts.util.ProcessGroup;
import org.technbolts.util.Strings;
import org.technbolts.util.Visitor;

import fj.Equal;
import fj.F;
import fj.Ord;

public class MarkingStoryValidator {
    public static final String MARKER_ID = Activator.PLUGIN_ID + ".storyMarker";
    
    private static Logger log = LoggerFactory.getLogger(MarkingStoryValidator.class);


    private IFile file;
    private IDocument document;
    private IProject project;

    public MarkingStoryValidator(IProject project, IFile file, IDocument document) {
        super();
        this.project = project;
        this.file = file;
        this.document = document;
    }

    public void removeExistingMarkers() {
        try {
            file.deleteMarkers(MARKER_ID, true, IResource.DEPTH_ZERO);
        } catch (CoreException e1) {
            Activator.logError("MarkingStoryValidator:Error while deleting existing marks", e1);
        }
    }

    public void validate() {
        List<StoryPart> parts = StoryPartDocumentUtils.getStoryParts(document);
        analyzeParts(parts);
    }

    private void analyzeParts(final List<StoryPart> storyParts) {
        final fj.data.List<Part> parts = fj.data.List.iterableList(storyParts).map(new F<StoryPart,Part>() {
            @Override
            public Part f(StoryPart storyPart) {
                return new Part(storyPart);
            }
        });
        
        ProcessGroup<?> group = Activator.getDefault().newProcessGroup();
        group.spawn(checkStepsAsRunnable(parts));
        group.spawn(checkNarrativeAsRunnable(parts));

        try {
            group.awaitTermination();
        } catch (InterruptedException e) {
            Activator.logError("MarkingStoryValidator:Error while checking steps for parts: " + parts, e);
        }

        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                for (Part part : parts)
                    part.applyMarks();
            }
        };
        try {
            file.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
        } catch (CoreException e) {
            Activator.logError("MarkingStoryValidator:Error while applying marks on <" + file + ">", e);
        }
    }
    
    private Runnable checkNarrativeAsRunnable(final fj.data.List<Part> parts) {
        return new Runnable() {
            public void run() {
                try {
                    checkNarrative(parts);
                } catch (Throwable e) {
                    Activator.logError("MarkingStoryValidator:Error while checking narrative for parts: " + parts, e);
                }
            }
        };
    }
    
    private void checkNarrative(final fj.data.List<Part> parts) throws JavaModelException {
        boolean nonNarrativeOrIgnorable = false;
        
        Part narrative = null;
        Part inOrderTo = null;
        Part asA = null;
        Part iWantTo = null;
        
        Iterator<Part> iterator = parts.iterator();
        while(iterator.hasNext()) {
            Part part = iterator.next();
            JBKeyword keyword = part.storyPart.getPreferredKeyword();
            if(keyword==null) {
                continue;
            }
            if(keyword.isNarrative()) {
                // narrative must be the first
                if(nonNarrativeOrIgnorable) {
                    part.addErrorMark(Marks.Code.InvalidNarrativePosition, "Narrative section must be the first one");
                }
                else {
                    switch(keyword) {
                        case Narrative:
                            if(narrative!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleNarrative, "Only one 'Narrative:' element is allowed");
                            else
                                narrative = part;
                            break;
                        case InOrderTo:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleInOrderTo, "Only one 'In order to ' element is allowed");
                            else
                                inOrderTo = part;
                            break;
                        case AsA:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to ' element");
                            else if(asA!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleAsA, "Only one 'As a ' element is allowed");
                            else
                                asA = part;
                            break;
                        case IWantTo:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to ' element");
                            else if(asA==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a ' element");
                            else if(iWantTo!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleIWantTo, "Only one 'I want to ' element is allowed");
                            else
                                iWantTo = part;
                            break;
                    }
                }
            }
            else if( keyword != JBKeyword.Ignorable
                  && keyword != JBKeyword.Meta
                  && keyword != JBKeyword.MetaProperty
                  && keyword != JBKeyword.GivenStories) {
                nonNarrativeOrIgnorable = true;
            }
        }
        
        // consolidation
        if(narrative!=null) {
            if(inOrderTo!=null) {
                if(asA!=null) {
                    if(iWantTo==null) {
                      asA.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to ' element");
                    }
                }
                else {
                    inOrderTo.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a ' element");
                }
            }
            else {
                narrative.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to ' element");
            }
        }
        
    }
    
    private Runnable checkStepsAsRunnable(final fj.data.List<Part> parts)  {
        return new Runnable() {
            public void run() {
                try {
                    checkSteps(parts);
                } catch (Throwable e) {
                    Activator.logError("MarkingStoryValidator:Error while checking steps for parts: " + parts, e);
                }
            }
        };
    }

    private void checkSteps(final fj.data.List<Part> parts) throws JavaModelException {
        fj.data.List<Part> steps = parts.filter(new F<Part,Boolean>() {
            public Boolean f(Part part) {
                return JBKeyword.isStep(part.storyPart.getPreferredKeyword());
            };
        });
        
        final Map<String, List<PotentialStep>> potentials = New.hashMap();
        for (Part part : steps) {
            List<PotentialStep> list = New.arrayList();
            String stepSentence = part.extractStepSentenceAndRemoveTrailingNewlines();
            log.debug("To analyse: >>" + Strings.escapeNL(stepSentence) + "<<");
            potentials.put(stepSentence, list);
        }

        StepLocator locator = StepLocator.getStepLocator(project);
        locator.traverseSteps(new Visitor<PotentialStep, Object>() {
            @Override
            public void visit(PotentialStep candidate) {
                for (String searched : potentials.keySet()) {
                    if (candidate.matches(searched)) {
                        potentials.get(searched).add(candidate);
                    }
                }
            }
        });

        for (Part part : steps) {
            String key = part.extractStepSentenceAndRemoveTrailingNewlines();
            List<PotentialStep> candidates = potentials.get(key);
            int count = candidates.size();
            log.debug("#" + count + "result(s) found for >>" + Strings.escapeNL(key) + "<<");
            if (count == 0)
                part.addErrorMark(Marks.Code.NoMatchingStep, "No step is matching <" + key + ">");
            else if (count > 1) {
                
                fj.data.List<Integer> collectedPrios = iterableList(candidates).map(new PotentialStepPrioTransformer());
                int max = collectedPrios.maximum(Ord.intOrd);
                int countWithMax = collectedPrios.filter(Equal.intEqual.eq(max)).length();
                if (countWithMax>1) {
                    part.addErrorMark(Marks.Code.MultipleMatchingSteps, "Ambiguous step: " + count + " steps are matching <" + key + "> got: " + candidates);
                }
                else {
                    part.addInfoMark(Marks.Code.MultipleMatchingSteps_PrioritySelection, 
                            "Multiple steps matching, but only one with the highest priority for <" + key + ">");
                    log.debug("#{} matching steps but only one with the highest priority for {}", candidates.size(), key);
                }
            }
        }
    }

    static final class PotentialStepPrioTransformer extends F<PotentialStep,Integer> {
        @Override
        public Integer f(PotentialStep pStep) {
            try {
                Integer prioValue = JBehaveProject.getValue(pStep.annotation.getMemberValuePairs(), "priority");
                return prioValue == null ? Integer.valueOf(0) : prioValue;
            } catch (JavaModelException e) {
                return Integer.valueOf(0);
            }
        }
    }

    class Part {
        private List<MarkData> marks = New.arrayList();
        private StoryPart storyPart;

        private Part(StoryPart storyPart) {
            super();
            this.storyPart = storyPart;
        }

        public String extractStepSentenceAndRemoveTrailingNewlines() {
            String stepSentence = storyPart.extractStepSentence();
            // remove any comment that can still be within the step
            String cleaned = Constants.removeComment(stepSentence);
            return Strings.removeTrailingNewlines(cleaned);
        }

        public synchronized void addErrorMark(Marks.Code code, String message) {
            addMark(code, message, IMarker.SEVERITY_ERROR);
        }
        
        public synchronized void addInfoMark(Marks.Code code, String message) {
            addMark(code, message, IMarker.SEVERITY_INFO);
        }
        
        public synchronized void addMark(Marks.Code code, String message, int severity) {
            MarkData markData = new MarkData()//
                    .severity(severity)//
                    .message(message)//
                    .offsetStart(storyPart.getOffsetStart())//
                    .offsetEnd(storyPart.getOffsetEnd());
            Marks.putCode(markData, code);
            marks.add(markData);
        }

        public void applyMarks() {
            if (marks.isEmpty())
                return;

            try {
                for (MarkData mark : marks) {
                    IMarker marker = file.createMarker(MARKER_ID);
                    marker.setAttributes(mark.createAttributes(file, document));
                    JBKeyword keyword = storyPart.getPreferredKeyword();
                    if(keyword!=null)
                        marker.setAttribute("Keyword", keyword.name());
                }
            } catch (Exception e) {
                Activator.logError("MarkingStoryValidator:Failed to apply marks", e);
            }
        }

        public String text() {
            return storyPart.getContent();
        }

        public String textWithoutTrailingNewlines() {
            return Strings.removeTrailingNewlines(text());
        }

        @Override
        public String toString() {
            return "Part [offset=" + storyPart.getOffset() + ", length=" + storyPart.getLength() + ", keyword=" + storyPart.getPreferredKeyword() + ", marks="
                    + marks + ", text=" + textWithoutTrailingNewlines() + "]";
        }

    }
}
