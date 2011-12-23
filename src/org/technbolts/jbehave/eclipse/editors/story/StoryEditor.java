package org.technbolts.jbehave.eclipse.editors.story;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.technbolts.eclipse.util.ColorManager;
import org.technbolts.eclipse.util.TemplateUtils;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.editors.EditorActionDefinitionIds;
import org.technbolts.jbehave.eclipse.editors.EditorMessages;
import org.technbolts.jbehave.eclipse.editors.story.actions.JumpToDeclarationAction;
import org.technbolts.jbehave.eclipse.editors.story.actions.ShowOutlineAction;
import org.technbolts.jbehave.eclipse.editors.story.completion.StoryContextType;
import org.technbolts.jbehave.eclipse.editors.story.completion.StoryTemplateProposal;
import org.technbolts.jbehave.eclipse.util.StepLocator;
import org.technbolts.jbehave.eclipse.util.StepUtils;
import org.technbolts.util.Strings;
import org.technbolts.util.Visitor;

public class StoryEditor extends TextEditor {

	private ColorManager colorManager;
    private ShowOutlineAction showOutline;
    private JumpToDeclarationAction jumpToDeclaration;

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
	protected void doSetInput(IEditorInput newInput) throws CoreException {
	    Activator.logInfo("StoryEditor.doSetInput()" + Strings.times(10, "**********\n"));
        super.doSetInput(newInput);
        validateAndMark();
    }

	@Override
	protected void setDocumentProvider(IEditorInput input) {
	    Activator.logInfo("StoryEditor.setDocumentProvider()" + Strings.times(10, "~~~~~~~~~\n"));
	    super.setDocumentProvider(input);
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
            if(document==null) {
                Activator.logInfo("StoryEditor.validateAndMark()" + Strings.times(10, "########\n"));
                return;
            }
            final IProject project = getInputFile().getProject();
            MarkingStoryValidator validator = new MarkingStoryValidator (project, getInputFile(), document);
            validator.removeExistingMarkers();
            validator.validate();
        }
        catch (Exception e)
        {
            Activator.logError("Failed to validate content", e);
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
    
    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "org.technbolts.jbehave.eclipse.storyEditorScope" }); //$NON-NLS-1$
    }
    
    @Override
    protected void createActions() {
        super.createActions();
        ResourceBundle bundle = EditorMessages.getBundleForConstructedKeys();
        
        showOutline = new ShowOutlineAction(bundle, "ShowOutline.", this);
        showOutline.setActionDefinitionId(EditorActionDefinitionIds.SHOW_OUTLINE);
        setAction(EditorActionDefinitionIds.SHOW_OUTLINE, showOutline);
        markAsContentDependentAction(EditorActionDefinitionIds.SHOW_OUTLINE, true);
        
        jumpToDeclaration = new JumpToDeclarationAction(bundle, "JumpToDeclaration.", this);
        jumpToDeclaration.setActionDefinitionId(EditorActionDefinitionIds.JUMP_TO_DECLARATION);
        setAction(EditorActionDefinitionIds.JUMP_TO_DECLARATION, jumpToDeclaration);
        markAsContentDependentAction(EditorActionDefinitionIds.JUMP_TO_DECLARATION, true);
    }

    public Iterable<PotentialStep> getPotentialSteps() {
        final IProject project = getInputFile().getProject();
        Visitor<PotentialStep, PotentialStep> collector = new Visitor<PotentialStep, PotentialStep>() {
            @Override
            public void visit(PotentialStep step) {
            }
        };
        try {
            StepLocator.getStepLocator(project).traverseSteps(collector);
        } catch (JavaModelException e) {
            Activator.logError("Failed to collect PotentialStep", e);
        }
        return collector.getFounds();
    }

    public void insert(PotentialStep pStep) {
        Point point = getSourceViewer().getSelectedRange();
        try {
            getInputDocument().replace(point.x, 0, pStep.fullStep()+"\n");
        } catch (BadLocationException e) {
            Activator.logError("Failed to insert potential step", e);
        }
    }
    
    public void insertAsTemplate(PotentialStep pStep) {
        IDocument document = getInputDocument();
        
        Point point = getSourceViewer().getSelectedRange();
        
        Region replacementRegion = new Region(point.x, 0);
        TemplateContextType contextType = StoryContextType.getTemplateContextType();
        TemplateContext templateContext = new DocumentTemplateContext(contextType, document, replacementRegion.getOffset(), replacementRegion.getLength());
        
        String templateText = TemplateUtils.templatizeVariables(pStep.fullStep());
        Template template = new Template(
                pStep.stepPattern,
                pStep.fullStep(), 
                StoryContextType.STORY_CONTEXT_TYPE_ID, templateText, false);
        new StoryTemplateProposal(template,
                templateContext, replacementRegion, null, 0).apply(getSourceViewer(), (char)0, SWT.CONTROL, point.x);
    }

    public void jumpToMethod() {
        try {
            StepUtils.jumpToSelectionDeclaration(getSourceViewer());
        } catch (PartInitException e) {
            Activator.logError("Failed to jump to method", e);
        } catch (JavaModelException e) {
            Activator.logError("Failed to jump to method", e);
        }
    }
}
