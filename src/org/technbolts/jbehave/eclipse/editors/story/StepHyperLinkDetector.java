package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.swt.custom.StyledText;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.eclipse.util.UIUtils;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepLocator;

public class StepHyperLinkDetector implements IHyperlinkDetector {
    
    private IHyperlink[] NONE = null;//new IHyperlink[0];
    
    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer viewer, final IRegion region,
            boolean canShowMultipleHyperlinks) {
        StyledText widget = viewer.getTextWidget();
        final int lineNo = widget.getLineAtOffset(region.getOffset());
        final int lineOffset = widget.getOffsetAtLine(lineNo);
        
        // TODO add support for multi-line step
        final String line = viewer.getTextWidget().getLine(lineNo);
        final String step = LineParser.extractStepSentence(line);
        
        if(step==null)
            return NONE;

        IHyperlink link = new IHyperlink() {

            @Override
            public IRegion getHyperlinkRegion() {
                return new Region(lineOffset, line.length());
            }

            @Override
            public String getHyperlinkText() {
                return step;
            }

            @Override
            public String getTypeLabel() {
                return "Go to step";
            }

            @Override
            public void open() {
                try {
                    // configure search
                    IProject project = EditorUtils.findProject(viewer);
                    if (project == null) {
                        UIUtils.show("Step not found", "No project found.");
                        return;
                    }

                    IJavaElement methodToJump = new StepLocator().findMethod(step, project);
                    // jump to method
                    if (methodToJump != null) {
                        JavaUI.openInEditor(methodToJump);
                        methodToJump = null;
                    } else {
                        UIUtils.show("Step not found", "There is no step matching:\n" + step);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return new IHyperlink[] { link };
    }

}
