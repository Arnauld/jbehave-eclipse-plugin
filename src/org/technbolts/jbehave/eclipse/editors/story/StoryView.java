package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

public class StoryView extends ViewPart {

    public static final String ID = "org.technbolts.jbehave.eclipse.editors.story.StoryView"; //$NON-NLS-1$
    private Label storylanguagelabel;

    public StoryView() {
    }
    
    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        site.getPage().addPartListener(new IPartListener2() {
            
            @Override
            public void partVisible(IWorkbenchPartReference partRef) {
            }
            
            @Override
            public void partOpened(IWorkbenchPartReference partRef) {
                changeContent(partRef);
            }
            
            @Override
            public void partInputChanged(IWorkbenchPartReference partRef) {
                changeContent(partRef);
            }
            
            @Override
            public void partHidden(IWorkbenchPartReference partRef) {
            }
            
            @Override
            public void partDeactivated(IWorkbenchPartReference partRef) {
            }
            
            @Override
            public void partClosed(IWorkbenchPartReference partRef) {
                clearContent(partRef);
            }
            
            @Override
            public void partBroughtToTop(IWorkbenchPartReference partRef) {
                changeContent(partRef);
            }
            
            @Override
            public void partActivated(IWorkbenchPartReference partRef) {
                changeContent(partRef);                
            }
        });
    }
    
    private void clearContent (IWorkbenchPartReference partRef) {
        if(!isInterestedBy(partRef))
            return;
        storylanguagelabel.setText("n/a");
    }
    
    private void changeContent (IWorkbenchPartReference partRef) {
        if(!isInterestedBy(partRef))
            return;
        StoryEditor editorPart = (StoryEditor)partRef.getPart(true);
        storylanguagelabel.setText(editorPart.getJBehaveProject().getLocale().toString());
    }

    private boolean isInterestedBy(IWorkbenchPartReference partRef) {
        return StoryEditor.EDITOR_ID.equals(partRef.getId());
    }

    /**
     * Create contents of the view part.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        {
            Label lblStoryLanguage = new Label(container, SWT.NONE);
            lblStoryLanguage.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
            lblStoryLanguage.setText("Story language");
        }
        {
            storylanguagelabel = new Label(container, SWT.NONE);
            storylanguagelabel.setText("en");
        }

        createActions();
        initializeToolBar();
        initializeMenu();
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        // Create the actions
    }

    /**
     * Initialize the toolbar.
     */
    private void initializeToolBar() {
        //IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    }

    /**
     * Initialize the menu.
     */
    private void initializeMenu() {
        //IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
    }

    @Override
    public void setFocus() {
        // Set the focus
    }

}
