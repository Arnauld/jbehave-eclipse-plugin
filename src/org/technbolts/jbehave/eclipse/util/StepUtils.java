package org.technbolts.jbehave.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PartInitException;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.eclipse.util.UIUtils;

public class StepUtils {
    
    public static boolean jumpToSelectionDeclaration(final ITextViewer viewer) throws JavaModelException,
    PartInitException {
        StyledText widget = viewer.getTextWidget();
        
        Point point = viewer.getSelectedRange();
        final int lineNo = widget.getLineAtOffset(point.x);
        
        // TODO add support for multi-line step
        final String line = viewer.getTextWidget().getLine(lineNo);
        final String step = LineParser.extractStepSentence(line);
        
        if(step==null)
            return false;
        
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
