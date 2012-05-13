package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.technbolts.eclipse.util.ColorManager;
import org.technbolts.eclipse.util.ProjectAwareFastPartitioner;
import org.technbolts.eclipse.util.TemplateUtils;
import org.technbolts.eclipse.util.TextAttributeProvider;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.JBehaveProjectListener;
import org.technbolts.jbehave.eclipse.JBehaveProjectRegistry;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.editors.EditorActionDefinitionIds;
import org.technbolts.jbehave.eclipse.editors.EditorMessages;
import org.technbolts.jbehave.eclipse.editors.story.actions.JumpToDeclarationAction;
import org.technbolts.jbehave.eclipse.editors.story.actions.QuickSearchAction;
import org.technbolts.jbehave.eclipse.editors.story.actions.ShowOutlineAction;
import org.technbolts.jbehave.eclipse.editors.story.actions.ToggleCommentAction;
import org.technbolts.jbehave.eclipse.editors.story.completion.StoryContextType;
import org.technbolts.jbehave.eclipse.editors.story.outline.OutlineModel;
import org.technbolts.jbehave.eclipse.editors.story.outline.OutlineModelBuilder;
import org.technbolts.jbehave.eclipse.editors.story.outline.OutlineView;
import org.technbolts.jbehave.eclipse.preferences.PreferenceConstants;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.eclipse.textstyle.TextStylePreferences;
import org.technbolts.jbehave.eclipse.util.StepUtils;
import org.technbolts.util.Runnables;
import org.technbolts.util.Visitor;

import fj.Effect;

public class StoryEditor extends TextEditor {

    public static final String EDITOR_ID = "org.technbolts.jbehave.eclipse.editors.story.StoryEditor";

    private ColorManager colorManager;
    //
    private ShowOutlineAction showOutline;
    private JumpToDeclarationAction jumpToDeclaration;
    private QuickSearchAction quickSearch;
    private ToggleCommentAction toggleComment;
    //
    private IPropertyChangeListener listener;
    private JBehaveProjectListener projectListener;
    //
    private TextAttributeProvider textAttributeProvider;
    private Object outlineView;
    private StoryConfiguration storyConfiguration;

    public StoryEditor() {
        super();
        colorManager = new ColorManager();
        textAttributeProvider = new TextAttributeProvider(colorManager);
        textAttributeProvider.changeTheme(getTheme());
        storyConfiguration = new StoryConfiguration(this, textAttributeProvider);
        setSourceViewerConfiguration(storyConfiguration);
        setDocumentProvider(new StoryDocumentProvider());
    }

    public void oops() {
        //ITextListener listener = storyConfiguration.getReconcilerInternalListener(ITextListener.class);
        //TextEvent event = new TextEvent(0, 0, null, null, null, true) {};
        //listener.textChanged(event);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        listener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                updateStyles();
            }

        };
        getStore().addPropertyChangeListener(listener);
    }

    private void updateStyles() {
        TextStyle theme = getTheme();
        textAttributeProvider.changeTheme(theme);

        StyledText textWidget = getSourceViewer().getTextWidget();
        textWidget.setBackground(colorManager.getColor(theme.getBackgroundOrDefault()));
        textWidget.setForeground(colorManager.getColor(theme.getForegroundOrDefault()));

        adjustCurrentLineColor(theme);
        getSourceViewer().invalidateTextPresentation();
    }

    private static TextStyle getTheme() {
        return TextStylePreferences.getTheme(getStore());
    }

    @Override
    public void dispose() {
        getStore().removePropertyChangeListener(listener);
        colorManager.dispose();
        getJBehaveProject().removeListener(projectListener);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        updateStyles();
    }

    private static boolean AttemptToChangeCurrentLineColorAccordingToTheme = true;

    private void adjustCurrentLineColor(TextStyle theme) {
        if (AttemptToChangeCurrentLineColorAccordingToTheme) {
            PreferenceConverter.setValue(//
                    getPreferenceStore(), //
                    PreferenceConstants.CUSTOM_CURRENT_LINE_COLOR, theme.getCurrentLineHighlight());
        }
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);
        if (AttemptToChangeCurrentLineColorAccordingToTheme) {
            adjustCurrentLineColor(getTheme());
            support.setCursorLinePainterPreferenceKeys(
                    AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE,
                    PreferenceConstants.CUSTOM_CURRENT_LINE_COLOR);
        }
    }

    private static IPreferenceStore getStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    @Override
    protected void doSetInput(IEditorInput newInput) throws CoreException {
        super.doSetInput(newInput);
        registerJBehaveProjectListener();
        validateAndMark();
    }

    private void registerJBehaveProjectListener() {
        if (projectListener != null)
            getJBehaveProject().removeListener(projectListener);
        else
            projectListener = new JBehaveProjectListener() {
                @Override
                public void stepsInvalidated() {
                    validateAndMark();
                    invalidateTextPresentation();
                }
            };
        getJBehaveProject().addListener(projectListener);
    }

    private void invalidateTextPresentation() {
        // make sure to invalidate in the Display thread.
        getSourceViewer().getTextWidget().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                getSourceViewer().invalidateTextPresentation();
            }
        });
    }

    @Override
    protected void editorSaved() {
        super.editorSaved();

        ProjectAwareFastPartitioner partitioner = (ProjectAwareFastPartitioner) getInputDocument()
                .getDocumentPartitioner();
        if (partitioner != null) {
            partitioner.invalidatePartitions();
        }
        validateAndMark();
    }

    protected void validateAndMark() {
        try {
            IDocument document = getInputDocument();
            if (document == null) {
                return;
            }
            MarkingStoryValidator validator = new MarkingStoryValidator(getJBehaveProject(), getInputFile(), document);
            validator.removeExistingMarkers();
            validator.validate(Runnables.noop());
        } catch (Exception e) {
            Activator.logError("Failed to validate content", e);
        }
    }

    public IDocument getInputDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    protected IFile getInputFile() {
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

        quickSearch = new QuickSearchAction(bundle, "QuickSearch.", this);
        quickSearch.setActionDefinitionId(EditorActionDefinitionIds.QUICK_SEARCH);
        setAction(EditorActionDefinitionIds.QUICK_SEARCH, quickSearch);
        markAsContentDependentAction(EditorActionDefinitionIds.QUICK_SEARCH, true);

        jumpToDeclaration = new JumpToDeclarationAction(bundle, "JumpToDeclaration.", this);
        jumpToDeclaration.setActionDefinitionId(EditorActionDefinitionIds.JUMP_TO_DECLARATION);
        setAction(EditorActionDefinitionIds.JUMP_TO_DECLARATION, jumpToDeclaration);
        markAsContentDependentAction(EditorActionDefinitionIds.JUMP_TO_DECLARATION, true);

        toggleComment = new ToggleCommentAction(bundle, "ToggleComment.", this);
        toggleComment.setActionDefinitionId(EditorActionDefinitionIds.TOGGLE_COMMENT);
        setAction(EditorActionDefinitionIds.TOGGLE_COMMENT, toggleComment);
        markAsContentDependentAction(EditorActionDefinitionIds.JUMP_TO_DECLARATION, true);

    }

    public Iterable<PotentialStep> getPotentialSteps() {
        Visitor<PotentialStep, PotentialStep> collector = new Visitor<PotentialStep, PotentialStep>() {
            @Override
            public void visit(PotentialStep step) {
                add(step);
            }
        };
        try {
            getJBehaveProject().traverseSteps(collector);
        } catch (JavaModelException e) {
            Activator.logError("Failed to collect PotentialStep", e);
        }
        return collector.getFounds();
    }

    public JBehaveProject getJBehaveProject() {
        final IProject project = getInputFile().getProject();
        return JBehaveProjectRegistry.get().getOrCreateProject(project);
    }

    public void insert(PotentialStep pStep) {
        Point point = getSourceViewer().getSelectedRange();
        try {
            getInputDocument().replace(point.x, 0, pStep.fullStep() + "\n");
        } catch (BadLocationException e) {
            Activator.logError("Failed to insert potential step", e);
        }
    }

    public void insertAsTemplate(PotentialStep pStep) {
        IDocument document = getInputDocument();

        Point point = getSourceViewer().getSelectedRange();
        int lineNo = getSourceViewer().getTextWidget().getLineAtOffset(point.x);
        int lineOffset = getSourceViewer().getTextWidget().getOffsetAtLine(lineNo);

        Region replacementRegion = new Region(lineOffset, 0);
        TemplateContextType contextType = StoryContextType.getTemplateContextType();
        TemplateContext templateContext = new DocumentTemplateContext(contextType, document,
                replacementRegion.getOffset(), replacementRegion.getLength());

        String templateText = TemplateUtils.templatizeVariables(pStep.fullStep()) + "\n";
        Template template = new Template(pStep.stepPattern, pStep.fullStep(), StoryContextType.STORY_CONTEXT_TYPE_ID,
                templateText, false);
        new TemplateProposal(template, templateContext, replacementRegion, null, 0).apply(getSourceViewer(), (char) 0,
                SWT.CONTROL, replacementRegion.getOffset());
    }

    public void jumpToMethod() {
        try {
            new StepUtils(getJBehaveProject()).jumpToSelectionDeclaration(getSourceViewer());
        } catch (PartInitException e) {
            Activator.logError("Failed to jump to method", e);
        } catch (JavaModelException e) {
            Activator.logError("Failed to jump to method", e);
        }
    }

    public void applyChange(Effect<ISourceViewer> e) {
        ISourceViewer sourceViewer = getSourceViewer();
        beginCompoundChange(sourceViewer);
        try {
            e.e(sourceViewer);
        } finally {
            endCompoundChange(sourceViewer);
        }
    }

    private static void endCompoundChange(ITextViewer viewer) {
        if (viewer instanceof ITextViewerExtension) {
            ITextViewerExtension extension = (ITextViewerExtension) viewer;
            IRewriteTarget target = extension.getRewriteTarget();
            target.endCompoundChange();
        }
    }

    private static void beginCompoundChange(ITextViewer viewer) {
        if (viewer instanceof ITextViewerExtension) {
            ITextViewerExtension extension = (ITextViewerExtension) viewer;
            IRewriteTarget target = extension.getRewriteTarget();
            target.beginCompoundChange();
        }
    }

    public IProject getProject() {
        return getInputFile().getProject();
    }

    public void showRange(int offset, int length) {
        getSourceViewer().revealRange(offset, length);
        getSourceViewer().setRangeIndication(offset, length, true);
    }

    public List<OutlineModel> getOutlineModels() {
        OutlineModelBuilder builder = new OutlineModelBuilder(getLocalizedStepSupport(), getInputDocument());
        return builder.build();
    }

    protected LocalizedStepSupport getLocalizedStepSupport() {
        return getJBehaveProject().getLocalizedStepSupport();
    }

    public void addTextListener(ITextListener textListener) {
        getSourceViewer().addTextListener(textListener);
    }

    public void removeTextListener(ITextListener textListener) {
        getSourceViewer().removeTextListener(textListener);
    }

    @Override
    public synchronized Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IContentOutlinePage.class)) {
            if (outlineView == null)
                outlineView = new OutlineView(this, Activator.getDefault().getImageRegistry());
            return outlineView;
        }
        return super.getAdapter(adapter);
    }

    public synchronized void outlinePageClosed() {
        outlineView = null;
    }

}
