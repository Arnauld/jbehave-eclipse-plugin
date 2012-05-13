package org.technbolts.jbehave.eclipse.editors.story.actions;

import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.editors.story.StoryEditor;
import org.technbolts.util.Strings;

import fj.Effect;

/**
 */
public class ToggleCommentAction extends TextEditorAction {

    public ToggleCommentAction(final ResourceBundle bundle, final String prefix, final ITextEditor editor) {
        super(bundle, prefix, editor);
    }

    public void run() {
        if (!(getTextEditor() instanceof StoryEditor)) {
            return;
        }
        final StoryEditor storyEditor = (StoryEditor) getTextEditor();
        storyEditor.applyChange(new Effect<ISourceViewer>() {
            @Override
            public void e(ISourceViewer viewer) {
                process(storyEditor, viewer);
            }
        });
        storyEditor.oops();
    }

    private void process(StoryEditor storyEditor, ISourceViewer viewer) {
        ISelectionProvider selectionProvider = viewer.getSelectionProvider();
        ITextSelection sel = (ITextSelection) selectionProvider.getSelection();
        IDocument doc = viewer.getDocument();
        JBehaveProject jbehaveProject = storyEditor.getJBehaveProject();
        String ignorable = jbehaveProject.getLocalizedStepSupport().getLocalizedKeywords().ignorable();

        try {
            int startLine = sel.getStartLine();
            int endLine = sel.getEndLine();
            
            // if one line is not empty and not commented
            // then one comments all the lines! even those already commented
            int notCommentedCount = 0;
            for (int lineNb = startLine; lineNb <= endLine; lineNb++) {
                int lineOffset = doc.getLineOffset(lineNb);
                int lineLength = doc.getLineLength(lineNb);
                String text = doc.get(lineOffset, lineLength);
                if (StringUtils.isNotBlank(text) && !text.startsWith(ignorable))
                    notCommentedCount++;
            }
            
            for (int lineNb = startLine; lineNb <= endLine; lineNb++) {
                int lineOffset = doc.getLineOffset(lineNb);
                int lineLength = doc.getLineLength(lineNb);
                String text = doc.get(lineOffset, lineLength);

                if (notCommentedCount > 0) {
                    text = ignorable + " " + text;
                } else if (text.startsWith(ignorable)) {
                    text = text.substring(ignorable.length());
                    text = Strings.removeLeftSpaces(text);
                }
                doc.replace(lineOffset, lineLength, text);
            }

            int begOffset = doc.getLineOffset(startLine);
            int endOffset = doc.getLineOffset(endLine) + doc.getLineLength(endLine);
            selectionProvider.setSelection(new TextSelection(begOffset, endOffset - begOffset - 1));
        } catch (BadLocationException e) {
            Activator.logError("Erf!", e);
        }
    }
}
