package org.technbolts.jbehave.eclipse.editors.story;

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
import org.technbolts.eclipse.util.MarkData;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.New;
import org.technbolts.util.ProcessGroup;
import org.technbolts.util.Strings;
import org.technbolts.util.Visitor;

import fj.F;

public class MarkingStoryValidator {
    public static final String MARKER_ID = Activator.PLUGIN_ID + ".storyMarker";

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
                    part.addMark(Marks.InvalidNarrativePosition, "Narrative section must be the first one");
                }
                else {
                    switch(keyword) {
                        case Narrative:
                            if(narrative!=null)
                                part.addMark(Marks.InvalidNarrativeSequence_multipleNarrative, "Only one 'Narrative:' element is allowed");
                            else
                                narrative = part;
                            break;
                        case InOrderTo:
                            if(narrative==null)
                                part.addMark(Marks.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo!=null)
                                part.addMark(Marks.InvalidNarrativeSequence_multipleInOrderTo, "Only one 'In order to ' element is allowed");
                            else
                                inOrderTo = part;
                            break;
                        case AsA:
                            if(narrative==null)
                                part.addMark(Marks.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo==null)
                                part.addMark(Marks.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to ' element");
                            else if(asA!=null)
                                part.addMark(Marks.InvalidNarrativeSequence_multipleAsA, "Only one 'As a ' element is allowed");
                            else
                                asA = part;
                            break;
                        case IWantTo:
                            if(narrative==null)
                                part.addMark(Marks.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo==null)
                                part.addMark(Marks.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to ' element");
                            else if(asA==null)
                                part.addMark(Marks.InvalidNarrativeSequence_missingAsA, "Missing 'As a ' element");
                            else if(iWantTo!=null)
                                part.addMark(Marks.InvalidNarrativeSequence_multipleIWantTo, "Only one 'I want to ' element is allowed");
                            else
                                iWantTo = part;
                            break;
                    }
                }
            }
            else if(keyword != JBKeyword.Ignorable) {
                nonNarrativeOrIgnorable = true;
            }
        }
        
        // consolidation
        if(narrative!=null) {
            if(inOrderTo!=null) {
                if(asA!=null) {
                    if(iWantTo==null) {
                      asA.addMark(Marks.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to ' element");
                    }
                }
                else {
                    inOrderTo.addMark(Marks.InvalidNarrativeSequence_missingAsA, "Missing 'As a ' element");
                }
            }
            else {
                narrative.addMark(Marks.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to ' element");
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
            potentials.put(extractStepSentenceAndRemoveTrailingNewlines(part), list);
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
            String key = extractStepSentenceAndRemoveTrailingNewlines(part);
            List<PotentialStep> candidates = potentials.get(key);
            int count = candidates.size();
            if (count == 0)
                part.addMark(Marks.NoMatchingStep, "No step is matching <" + key + ">");
            else if (count > 1)
                part.addMark(Marks.MultipleMatchingSteps, "Ambiguous step: " + count + " steps are matching <" + key + "> got: "
                        + candidates);
        }
    }

    private static String extractStepSentenceAndRemoveTrailingNewlines(Part part) {
        return Strings.removeTrailingNewlines(LineParser.extractStepSentence(part.text()));
    }

    class Part {
        private List<MarkData> marks = New.arrayList();
        private StoryPart storyPart;

        private Part(StoryPart storyPart) {
            super();
            this.storyPart = storyPart;
        }

        public synchronized void addMark(int code, String message) {
            marks.add(new MarkData()//
                    .severity(IMarker.SEVERITY_ERROR)//
                    .message(message)//
                    .offsetStart(storyPart.getOffsetStart())//
                    .offsetEnd(storyPart.getOffsetEnd())
                    .attribute(Marks.ERROR_CODE, code));
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
