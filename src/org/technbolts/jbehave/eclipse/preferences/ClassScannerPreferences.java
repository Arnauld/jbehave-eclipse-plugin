package org.technbolts.jbehave.eclipse.preferences;

import static org.technbolts.jbehave.eclipse.preferences.ClassScannerFilterEntry.filter;
import static org.technbolts.jbehave.eclipse.preferences.ClassScannerFilterEntry.toPatterns;
import static org.technbolts.jbehave.eclipse.preferences.ClassScannerFilterEntry.toSplittedPatterns;
import static org.technbolts.util.FJ.listCollector;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.technbolts.eclipse.preferences.PreferencesHelper;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.preferences.ClassScannerFilterEntry.ApplyOn;

import fj.Equal;
import fj.data.List;

public class ClassScannerPreferences {
    private static final String QUALIFIER = Activator.PLUGIN_ID + "/classScanner";
    
    public static final String EXCLUDE_SUFFIX = "-excludes";
    public static final String INCLUDE_SUFFIX = "-includes";
    //
    public static final String USE_PROJECT_SETTINGS = "use_project_settings";
    
    private final PreferencesHelper helper;
    private List<ClassScannerFilterEntry> entries = List.nil();
    private boolean useProjectSettings;

    public ClassScannerPreferences(IScopeContext scope) {
        helper = PreferencesHelper.getHelper(QUALIFIER, scope);
    }
    
    public ClassScannerPreferences() {
        helper = PreferencesHelper.getHelper(QUALIFIER);
    }

    public ClassScannerPreferences(final IProject project) {
        helper = PreferencesHelper.getHelper(QUALIFIER, project);
    }
    
    public String[] getPackageRootExcludes() {
        return getSplittedStrings(ApplyOn.PackageRoot, true);
    }

    public String[] getPackageRootIncludes() {
        return getSplittedStrings(ApplyOn.PackageRoot, false);
    }

    public String[] getPackageExcludes() {
        return getSplittedStrings(ApplyOn.Package, true);
    }

    public String[] getPackageIncludes() {
        return getSplittedStrings(ApplyOn.Package, false);
    }

    public String[] getClassExcludes() {
        return getSplittedStrings(ApplyOn.Class, true);
    }

    public String[] getClassIncludes() {
        return getSplittedStrings(ApplyOn.Class, false);
    }
    
    private String[] getSplittedStrings(ApplyOn applyOn, boolean exclude) {
        List<String> patterns = entries
            .filter(filter(applyOn, exclude))
            .map(toSplittedPatterns())
            .foldLeft(listCollector(String.class), List.<String>nil());
        return patterns.toArray().array(String[].class);
    }

    public boolean hasOptionsAtLowestScope() {
        return helper.hasAnyAtLowestScope();
    }

    public void store() throws BackingStoreException {
        boolean[] modes = { true, false };
        
        for(ApplyOn applyOn : ApplyOn.values()) {
            for(Boolean mode : modes) {
                List<String> patternsList = entries.filter(filter(applyOn, mode)).map(toPatterns());
                String value = StringUtils.join(patternsList.toCollection(), "|");
                String key = applyOn +  (mode?EXCLUDE_SUFFIX:INCLUDE_SUFFIX);
                helper.putString(key, value);
            }
        }
        helper.putBoolean(USE_PROJECT_SETTINGS, useProjectSettings);
        helper.flush();
    }

    public void load() throws BackingStoreException {
        boolean[] modes = { true, false };

        entries = List.nil();
        for(ApplyOn applyOn : ApplyOn.values()) {
            for(Boolean mode : modes) {
                String key = applyOn +  (mode?EXCLUDE_SUFFIX:INCLUDE_SUFFIX);
                String inlinedPatterns = helper.getString(key, "");
                for(String patterns : inlinedPatterns.split("[\\|]")) {
                    if(StringUtils.isBlank(patterns))
                        continue;
                    entries = entries.snoc(new ClassScannerFilterEntry(patterns, applyOn, mode));
                }
            }
        }
        useProjectSettings = helper.getBoolean(USE_PROJECT_SETTINGS, false);
    }
    
    public ClassScannerFilterEntry addEntry(String patterns, ApplyOn applyOn, boolean exclude) {
        ClassScannerFilterEntry entry = new ClassScannerFilterEntry(patterns, applyOn, exclude);
        entries = entries.snoc(entry);
        //entries = entries.nub();//prevent duplicate
        return entry;
    }
    
    public void removeEntry(ClassScannerFilterEntry entry) {
        Equal<ClassScannerFilterEntry> any = Equal.anyEqual();
        entries = entries.removeAll(any.eq(entry));
    }
    
    public List<ClassScannerFilterEntry> getEntries() {
        return entries;
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

    public Object[] getEntriesAsObjectArray() {
        return entries.toCollection().toArray();
    }

}
