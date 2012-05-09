package org.technbolts.jbehave.eclipse.preferences;

import static org.technbolts.util.Objects.o;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.preferences.PreferencesHelper;
import org.technbolts.jbehave.eclipse.Activator;

public class ProjectPreferences {
    
    private Logger logger = LoggerFactory.getLogger(ProjectPreferences.class);
    
    private static final String QUALIFIER = Activator.PLUGIN_ID + "/project";
    
    private final PreferencesHelper helper;
    //
    public static final String USE_PROJECT_SETTINGS = "use_project_settings";
    public static final String LANGUAGES = "keyword.languages";  
    public static final String LANGUAGE = "keyword.language";
    
    private final boolean isProjectLevel;
    private boolean useProjectSettings;
    private String storyLanguage;

    private String[] availableStoryLanguages;

    public ProjectPreferences(IScopeContext scope) {
        helper = PreferencesHelper.getHelper(QUALIFIER, scope);
        isProjectLevel = false;
    }
    
    public ProjectPreferences() {
        helper = PreferencesHelper.getHelper(QUALIFIER);
        isProjectLevel = false;
    }

    public ProjectPreferences(final IProject project) {
        helper = PreferencesHelper.getHelper(QUALIFIER, project);
        isProjectLevel = true;
    }
    
    public void addListener(IPreferenceChangeListener changeListener) {
        helper.addListener(changeListener);
    }
    
    public boolean hasOptionsAtLowestScope() {
        return helper.hasAnyAtLowestScope();
    }

    public void store() throws BackingStoreException {
        helper.putString(LANGUAGE, storyLanguage);
        helper.putString(LANGUAGES, StringUtils.join(availableStoryLanguages,","));
        helper.putBoolean(USE_PROJECT_SETTINGS, useProjectSettings);
        helper.flush();
    }

    public void load() throws BackingStoreException {
        storyLanguage = helper.getString(LANGUAGE, "en");
        availableStoryLanguages = helper.getString(LANGUAGES, "en").split(",");
        useProjectSettings = helper.getBoolean(USE_PROJECT_SETTINGS, false);
        logger.info("Project preferences loaded (projectLevel: {}), storyLanguage: {}, useProjectSettings: {}",//
                o(isProjectLevel, storyLanguage, useProjectSettings));
    }
    
    public String[] availableStoryLanguages() {
        return availableStoryLanguages;
    }
    public void setAvailableStoryLanguages(String... availableStoryLanguages) {
        this.availableStoryLanguages = availableStoryLanguages;
    }
    
    public void setStoryLanguage(String storyLanguage) {
        this.storyLanguage = storyLanguage;
    }
    
    public String getStoryLanguage() {
        return storyLanguage;
    }
    
    public boolean isUseProjectSettings() {
        return useProjectSettings;
    }
    
    public void setUseProjectSettings(boolean useProjectSettings) {
        this.useProjectSettings = useProjectSettings;
    }
    
    public void removeAllSpecificSettings() throws BackingStoreException {
        helper.removeAllAtLowestScope();
        load();
    }

}
