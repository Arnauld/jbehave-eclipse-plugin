package org.technbolts.jbehave.eclipse.preferences;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.osgi.service.prefs.BackingStoreException;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.preferences.ClassScannerFilterEntry.ApplyOn;
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

        TextStyle darkTheme = TextStyleTheme.createDarkTheme();
        TextStylePreferences.storeAsDefault(darkTheme, store);

        TextStyle lightTheme = TextStyleTheme.createLightTheme();
        TextStylePreferences.storeAsDefault(lightTheme, store);

        store.setDefault(PreferenceConstants.THEMES, darkTheme.getPath() + "," + lightTheme.getPath());
        store.setDefault(PreferenceConstants.THEME, darkTheme.getPath());
        String[] resourceListing = {"de", "en", "fr", "it", "pt", "tr", "zh_TW"};
        store.setDefault(PreferenceConstants.LANGUAGES,StringUtils.join(resourceListing, ","));
        store.setDefault(PreferenceConstants.LANGUAGE,"en");
        
        store.setDefault(PreferenceConstants.CURRENT_LINE_ENABLED, true);
        PreferenceConverter.setDefault(store, PreferenceConstants.CUSTOM_CURRENT_LINE_COLOR, new RGB(70, 70, 70));

        ClassScannerPreferences classScannerPreferences = new ClassScannerPreferences(DefaultScope.INSTANCE);
        addEntries(classScannerPreferences, ApplyOn.Package, true,//
                "apple.*, com.apple.*, quicktime.*", //
                "sun.*, com.sun.*, sunw.*", //
                "java.*, javax.*", //
                "com.oracle.*", //
                "org.eclipse.*", //
                "com.google.common*", //
                "junit*, org.junit*", //
                "org.omg.*, org.xml.*, org.w3c.*", "org.ietf*, org.relaxng.*, org.jcp.*", //
                "org.codehaus.plexus*", //
                "fj*", //
                "org.xmlpull.*", // 
                "com.thoughtworks.xstream*, com.thoughtworks.paranamer*", // xstream
                "org.hamcrest*, org.mockito*, org.objenesis*", // mockito
                "org.apache.*", //
                "org.jbehave.*", //
                "freemarker*");
        
        try {
            classScannerPreferences.store();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to initialize default preferences for ClassScanner", e);
        }
    }

    private static void addEntries(ClassScannerPreferences prefs, ApplyOn applyOn, boolean exclude, String... patternsSeq) {
        for(String patterns : patternsSeq)
            prefs.addEntry(patterns, applyOn, exclude);
    }
}
