package org.technbolts.jbehave.eclipse.preferences;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wb.swt.ResourceManager;
import org.technbolts.jbehave.eclipse.Activator;

public class JBehavePluginPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private Text txtLocale;

	/**
	 * Create the preference page.
	 */
	public JBehavePluginPreferencePage() {
	}

	/**
	 * Create contents of the preference page.
	 * 
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		ResourceBundle bundle = PreferencesMessages.getBundle();

		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(2, false));

		Label lblLogo = new Label(container, SWT.NONE);
		lblLogo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				3, 1));
		lblLogo.setImage(ResourceManager.getPluginImage(
				"technbolts-jbehave-eclipse-plugin",
				"icons/jbehave-plugin-logo.png"));

		Label lblJbehaveEclipsePlugin = new Label(container, SWT.NONE);
		lblJbehaveEclipsePlugin.setLayoutData(new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 2, 1));
		lblJbehaveEclipsePlugin.setText(bundle
				.getString("jbehavePreferencePage.title"));

		Label lblVersion = new Label(container, SWT.NONE);
		String versionPattern = bundle
				.getString("jbehavePreferencePage.version");
		lblVersion.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblVersion.setText(MessageFormat.format(versionPattern, Activator
				.getDefault().getVersion()));

		Link lblPluginSite = new Link(container, SWT.NONE);
		lblPluginSite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblPluginSite.setText(bundle
				.getString("jbehavePreferencePage.pluginLink"));

		Link lblJbehaveSite = new Link(container, SWT.NONE);
		lblJbehaveSite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblJbehaveSite.setText(bundle
				.getString("jbehavePreferencePage.jbehaveLink"));

		Link lblIntroductionPage = new Link(container, SWT.NONE);
		lblIntroductionPage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		lblIntroductionPage.setText(bundle
				.getString("jbehavePreferencePage.prez"));

		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		label.setText("Language:");

		txtLocale = new Text(container, SWT.BORDER);
		txtLocale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		
		initialize();

		return container;
	}

	protected void initialize() {
		txtLocale.setText(getPreferenceStore().getString(PreferenceConstants.LOCALE));
	}

	@Override
	public boolean performOk() {
		storeModifications();
		return super.performOk();
	}

	
	protected void storeModifications() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(PreferenceConstants.LOCALE, txtLocale.getText());
	}

	public static Locale getLocale(IPreferenceStore store) {
	    String string = store.getString(PreferenceConstants.LOCALE);
	    if (string != null) {
	    	return new Locale(string);
	    }
		return Locale.ENGLISH;
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

}
