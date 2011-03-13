package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.technbolts.eclipse.util.ColorManager;
import org.technbolts.eclipse.util.EditorUtils;
import org.technbolts.eclipse.util.TextAttributeProvider;

public class StoryEditor extends TextEditor {

	private ColorManager colorManager;

	public StoryEditor() {
		super();
		colorManager = new ColorManager();
		TextAttributeProvider textAttributeProvider = new TextAttributeProvider(colorManager);
		setSourceViewerConfiguration(new StoryConfiguration(textAttributeProvider));
		setDocumentProvider(new StoryDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	@Override
	protected void doSetInput(IEditorInput newInput) throws CoreException
    {
        super.doSetInput(newInput);
        validateAndMark();
    }

	
	@Override
	protected void editorSaved() {
	    super.editorSaved();
	    validateAndMark();
	}
	
	protected void validateAndMark()
    {
        try
        {
            IDocument document = getInputDocument();
            MarkingStoryValidator validator = new MarkingStoryValidator (
                    EditorUtils.findProject(document), getInputFile(), document);
            validator.removeExistingMarkers();
            validator.validate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected IDocument getInputDocument()
    {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    protected IFile getInputFile()
    {
        IFileEditorInput ife = (IFileEditorInput) getEditorInput();
        IFile file = ife.getFile();
        return file;
    }
}
