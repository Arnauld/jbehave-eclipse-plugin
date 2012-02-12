package org.technbolts.jbehave.eclipse.preferences;

import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PropertyPage;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.osgi.service.prefs.BackingStoreException;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.ImageIds;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.LocaleUtils;

public class ProjectPreferencePage extends PropertyPage implements org.eclipse.ui.IWorkbenchPreferencePage {

    private Combo languageCombo;
    private Locale[] locales = { Locale.ENGLISH, Locale.ENGLISH };
    private Table table;
    private TableViewer localizedKeywords;
    private IProject project;
    private Button enableProjectSpecific;
    private ProjectPreferences prefs;
    private ControlEnableState blockEnableState;
    private Composite projectComposite;

    /**
     * Create the preference page.
     */
    public ProjectPreferencePage() {
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    @Override
    public Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(1, false));
        
        if (isProjectPreferencePage()) {
            enableProjectSpecific = new Button(container, SWT.CHECK);
            enableProjectSpecific.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
            enableProjectSpecific.setText("Enable project specific settings");
            enableProjectSpecific.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    adjustProjectSpecificState();
                }
                
                @Override
                public void widgetDefaultSelected(SelectionEvent event) {
                    adjustProjectSpecificState();
                }
            });
        }
        
        projectComposite = new Composite(container, SWT.NONE);
        projectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        projectComposite.setLayout(new GridLayout(2, false));
        
        Label lblStoryLanguage = new Label(projectComposite, SWT.NONE);
        lblStoryLanguage.setSize(87, 14);
        lblStoryLanguage.setText("Story Language");
        
        languageCombo = new Combo(projectComposite, SWT.READ_ONLY);
        languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        languageCombo.setSize(279, 22);
        languageCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
            @Override
            public void widgetSelected(SelectionEvent event) {
                int selectionIndex = languageCombo.getSelectionIndex();
                setLanguage(languageCombo.getItem(selectionIndex));
            }
        });
        new Label(projectComposite, SWT.NONE);
        new Label(projectComposite, SWT.NONE);
        
        localizedKeywords = new TableViewer(projectComposite, SWT.BORDER | SWT.FULL_SELECTION);
        localizedKeywords.setColumnProperties(new String[] {"English", "Selected" });
        localizedKeywords.setContentProvider(ArrayContentProvider.getInstance());
        localizedKeywords.setLabelProvider(new KeywordTableLabelProvider());
        localizedKeywords.setInput(JBKeyword.values());
        table = localizedKeywords.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        table.setSize(371, 289);
        
        reload();
        updatePageWithPrefs();
        
        return container;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.PropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public void setElement(final IAdaptable element) {
        project = (IProject) element.getAdapter(IResource.class);
        super.setElement(element);
    }

    private boolean isProjectPreferencePage() {
        return project != null;
    }

    private void setLanguage(String language) {
        this.locales[1] = LocaleUtils.createLocaleFromCode(language, Locale.ENGLISH);
        this.localizedKeywords.refresh(true);
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        // Initialize the preference page
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        try {
            updatePrefsWithPage();
            if (isProjectPreferencePage()
                    && !enableProjectSpecific.getSelection()) {
                prefs.removeAllSpecificSettings();
            } else {
                prefs.store();
            }
        } catch (final BackingStoreException e) {
            Activator.logError("Failed to store ClassScanner preferences", e);
        }
        return super.performOk();
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        try {
            prefs.removeAllSpecificSettings();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to remove specific settings", e);
        }
        reload();
        updatePageWithPrefs();
        super.performDefaults();
    }
    
    protected void performReload () {
        reload();
        updatePageWithPrefs();
        super.performDefaults();
    }

    private void reload() {
        if (project == null) {
            prefs = new ProjectPreferences();
        } else {
            prefs = new ProjectPreferences(project);
        }
        try {
            prefs.load();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to load preferences", e);
        }
    }
    
    private void updatePrefsWithPage() {
        if (isProjectPreferencePage()) {
            boolean isProjectSpecific = enableProjectSpecific.getSelection();
            prefs.setUseProjectSettings(isProjectSpecific);
            prefs.setStoryLanguage(locales[1].toString());
        }
    }

    private void updatePageWithPrefs() {
        String[] langs = prefs.availableStoryLanguages();
        String selectedLanguage = prefs.getStoryLanguage();
        languageCombo.setItems(langs);
        languageCombo.select(ArrayUtils.indexOf(langs, selectedLanguage));
        setLanguage(selectedLanguage);
        if (isProjectPreferencePage()) {
            enableProjectSpecific.setSelection(prefs.isUseProjectSettings());
            adjustProjectSpecificState();
        }
    }

    private void adjustProjectSpecificState() {
        boolean useProjectSpecificSettings = enableProjectSpecific.getSelection();
        if (useProjectSpecificSettings) {
            if (blockEnableState != null) {
                blockEnableState.restore();
                blockEnableState = null;
            }
        } else {
            if (blockEnableState == null) {
                blockEnableState = ControlEnableState.disable(projectComposite);
            }
        }
    }
    
    private class KeywordTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if(columnIndex==0) {
                switch((JBKeyword)element) {
                    case Given:
                        return Activator.getDefault().getImageRegistry().get(ImageIds.STEP_GIVEN);
                    case When:
                        return Activator.getDefault().getImageRegistry().get(ImageIds.STEP_WHEN);
                    case Then:
                        return Activator.getDefault().getImageRegistry().get(ImageIds.STEP_THEN);
                    case And:
                        return null; // TODO create image for AND
                    case GivenStories:
                    case Meta:
                    case MetaProperty:
                        return null; // TODO create generic image for meta 
                    case AsA:
                    case InOrderTo:
                    case IWantTo:
                    case Narrative:
                        return Activator.getDefault().getImageRegistry().get(ImageIds.NARRATIVE);
                    case ExamplesTable:
                    case ExamplesTableHeaderSeparator:
                    case ExamplesTableIgnorableSeparator:
                    case ExamplesTableRow:
                    case ExamplesTableValueSeparator:
                        return Activator.getDefault().getImageRegistry().get(ImageIds.EXAMPLE_TABLE);
                    case Scenario:
                        return Activator.getDefault().getImageRegistry().get(ImageIds.SCENARIO);
                    case Ignorable:
                        return null; // TODO create generic image for comment
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            return ((JBKeyword)element).asString(keywordsFor(columnIndex));
        }
    }

    private Keywords keywordsFor(int columnIndex) {
        return new LocalizedKeywords(locales[columnIndex]);
    }
    
}
