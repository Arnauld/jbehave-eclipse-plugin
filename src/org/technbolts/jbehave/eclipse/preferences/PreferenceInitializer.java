package org.technbolts.jbehave.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.jbehave.eclipse.textstyle.TextStylePreferences;
import org.technbolts.jbehave.eclipse.textstyle.TextStyleTheme;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		TextStyle darkTheme = TextStyleTheme.createDarkTheme ();
	    TextStylePreferences.storeAsDefault(darkTheme, store);

	    TextStyle lightTheme = TextStyleTheme.createLightTheme ();
        TextStylePreferences.storeAsDefault(lightTheme, store);

		store.setDefault(PreferenceConstants.THEMES, darkTheme.getPath() + "," + lightTheme.getPath());
        store.setDefault(PreferenceConstants.THEME,  darkTheme.getPath());
        
        store.setDefault(PreferenceConstants.CURRENT_LINE_ENABLED,  true);
        PreferenceConverter.setDefault(store, PreferenceConstants.CUSTOM_CURRENT_LINE_COLOR, new RGB(70,70,70));
	}
	
}
