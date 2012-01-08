package org.technbolts.jbehave.eclipse.util;


import static org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils.findStoryPartAtOffset;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PartInitException;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.eclipse.util.UIUtils;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.util.Ref;

public class StepUtils {
    
    public static boolean jumpToSelectionDeclaration(final ITextViewer viewer) throws JavaModelException,
    PartInitException {
        
        Point point = viewer.getSelectedRange();
        
        final Ref<StoryPart> found = findStoryPartAtOffset(viewer.getDocument(), point.x);
        if (found.isNull()) {
            return false;
        }
        
        final StoryPart part = found.get();
        if (!part.isStepPart())
            return false;
        final String step = part.extractStepSentenceAndRemoveTrailingNewlines();
        
        return jumpToDeclaration(viewer, step);
    }
    
    public static boolean jumpToDeclaration(final ITextViewer viewer, final String step) throws JavaModelException,
            PartInitException {
        // configure search
        IProject project = EditorUtils.findProject(viewer);
        if (project == null) {
            UIUtils.show("Step not found", "No project found.");
            return false;
        }

        IJavaElement methodToJump = StepLocator.getStepLocator(project).findMethod(step);
        // jump to method
        if (methodToJump != null) {
            JavaUI.openInEditor(methodToJump);
            return true;
        } else {
            UIUtils.show("Step not found", "There is no step matching:\n" + step);
            return false;
        }
    }
}