package org.technbolts.jbehave.eclipse.preferences;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wb.swt.ResourceManager;
import org.technbolts.jbehave.eclipse.Activator;

public class JBehavePluginPreferencePage extends PropertyPage implements IWorkbenchPreferencePage {

    /**
     * Create the preference page.
     */
    public JBehavePluginPreferencePage() {
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    @Override
    public Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        ResourceBundle bundle = PreferencesMessages.getBundle();

        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(1, false));
        
        Label lblLogo = new Label(container, SWT.NONE);
        lblLogo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        lblLogo.setImage(ResourceManager.getPluginImage("technbolts-jbehave-eclipse-plugin", "icons/jbehave-plugin-logo.png"));
        
        Label lblJbehaveEclipsePlugin = new Label(container, SWT.NONE);
        lblJbehaveEclipsePlugin.setText(bundle.getString("jbehavePreferencePage.title"));
        
        Label lblVersion = new Label(container, SWT.NONE);
        String versionPattern = bundle.getString("jbehavePreferencePage.version");
        lblVersion.setText(MessageFormat.format(versionPattern, Activator.getDefault().getVersion()));
        
        Link lblPluginSite = new Link(container, SWT.NONE);
        lblPluginSite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblPluginSite.setText(bundle.getString("jbehavePreferencePage.pluginLink"));
        
        Link lblJbehaveSite = new Link(container, SWT.NONE);
        lblJbehaveSite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblJbehaveSite.setText(bundle.getString("jbehavePreferencePage.jbehaveLink"));
        
        Link lblIntroductionPage = new Link(container, SWT.NONE);
        lblIntroductionPage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblIntroductionPage.setText(bundle.getString("jbehavePreferencePage.prez"));

        return container;
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        // Initialize the preference page
    }

}
