package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.jbehave.support.JBKeyword.stepFilter;
import static org.technbolts.util.Lists.filterTransformed;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.technbolts.eclipse.util.MarkData;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.StoryParser;
import org.technbolts.util.BidirectionalReader;
import org.technbolts.util.IO;
import org.technbolts.util.New;
import org.technbolts.util.ProcessGroup;
import org.technbolts.util.Strings;
import org.technbolts.util.Transform;
import org.technbolts.util.Visitor;

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
        List<Part> parts = extractParts();
        Activator.logInfo("MarkingStoryValidator:Validate parts found: " + StringUtils.join(parts, "\n\t"));
        analyzeParts(parts);
    }

    private List<Part> extractParts() {
        BidirectionalReader bidiReader = IO.toBidirectionalReader(document.get());

        List<Part> parts = New.arrayList();
        StoryParser parser = new StoryParser(false);
        JBKeyword keyword = null;
        do {
            int beg = bidiReader.getPosition();
            keyword = parser.nextKeyword(bidiReader);
            int end = bidiReader.getPosition();
            if(bidiReader.eof())
                end--;

            if (keyword == null)
                continue;

            Part part = new Part(keyword, beg, end);
            parts.add(part);
        } while (!bidiReader.eof());
        return parts;
    }

    private void analyzeParts(final List<Part> parts) {
        ProcessGroup<?> group = Activator.getDefault().newProcessGroup();
        group.spawn(new Runnable() {
            public void run() {
                try {
                    Activator.logInfo("MarkingStoryValidator:Checking steps");
                    checkSteps(parts);
                    Activator.logInfo("MarkingStoryValidator:Steps checked");
                } catch (Throwable e) {
                    Activator.logError("MarkingStoryValidator:Error while checking steps for parts: " + parts, e);
                }
            }
        });

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

    private void checkSteps(final List<Part> parts) throws JavaModelException {
        List<Part> steps = filterTransformed(parts, partToKeyword(), stepFilter());
        final Map<String, List<PotentialStep>> potentials = New.hashMap();
        for (Part part : steps) {
            List<PotentialStep> list = New.arrayList();
            potentials.put(extractStepSentenceAndRemoveTrailingNewlines(part), list);
        }

        Activator.logInfo("MarkingStoryValidator:checkSteps:Initializing locator");
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

        Activator.logInfo("MarkingStoryValidator:checkSteps:Analysing potentials on #" + steps.size() +" part(s)");
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
        Activator.logInfo("MarkingStoryValidator:checkSteps:Analysis done!");
    }

    private static String extractStepSentenceAndRemoveTrailingNewlines(Part part) {
        return Strings.removeTrailingNewlines(LineParser.extractStepSentence(part.text()));
    }

    public static Transform<Part, JBKeyword> partToKeyword() {
        return new Transform<MarkingStoryValidator.Part, JBKeyword>() {
            @Override
            public JBKeyword transform(Part part) {
                return part.keyword;
            }
        };
    }

    class Part {
        public final int offsetBeg;
        public final int offsetEnd;
        public final JBKeyword keyword;
        private List<MarkData> marks = New.arrayList();

        private Part(JBKeyword keyword, int offsetBeg, int offsetEnd) {
            super();
            this.keyword = keyword;
            this.offsetBeg = offsetBeg;
            this.offsetEnd = offsetEnd;
        }

        public void addMark(int code, String message) {
            marks.add(new MarkData()//
                    .severity(IMarker.SEVERITY_ERROR)//
                    .message(message)//
                    .offsetStart(offsetBeg)//
                    .offsetEnd(offsetEnd)
                    .attribute(Marks.ERROR_CODE, code));
        }

        public void applyMarks() {
            if (marks.isEmpty())
                return;

            try {
                for (MarkData mark : marks) {
                    IMarker marker = file.createMarker(MARKER_ID);
                    marker.setAttributes(mark.createAttributes(file, document));
                    marker.setAttribute("Keyword", keyword.name());
                }
            } catch (Exception e) {
                Activator.logError("MarkingStoryValidator:Failed to apply marks", e);
            }
        }

        public String text() {
            try {
                return document.get(offsetBeg, offsetEnd - offsetBeg);
            } catch (BadLocationException e) {
                Activator.logError("MarkingStoryValidator:Unable to get text using offset range <" + offsetBeg + ", " + offsetEnd + "> " +
                		"document length: " + document.getLength(), e);
            }
            return "";
        }

        public String textWithoutTrailingNewlines() {
            return Strings.removeTrailingNewlines(text());
        }

        @Override
        public String toString() {
            return "Part [offsetBeg=" + offsetBeg + ", offsetEnd=" + offsetEnd + ", keyword=" + keyword + ", marks="
                    + marks + ", text=" + textWithoutTrailingNewlines() + "]";
        }

    }
}
