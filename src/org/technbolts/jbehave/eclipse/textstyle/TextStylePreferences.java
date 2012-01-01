package org.technbolts.jbehave.eclipse.textstyle;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

public class TextStylePreferences {
    
    public static void load(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            style.setItalic(store.getBoolean(path+".italic"));
            style.setBold(store.getBoolean(path+".bold"));
            
            boolean hasForeground = store.getBoolean(path+".foreground?");
            if(hasForeground)
                style.setForeground(PreferenceConverter.getColor(store, path+".foreground"));
            else
                style.setForeground(null);
            
            boolean hasBackground = store.getBoolean(path+".background?");
            if(hasBackground)
                style.setBackground(PreferenceConverter.getColor(store, path+".background"));
            else
                style.setBackground(null);
        }
    }
    
    public static void loadFromDefault(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            style.setItalic(store.getDefaultBoolean(path+".italic"));
            style.setBold(store.getDefaultBoolean(path+".bold"));
            
            boolean hasForeground = store.getDefaultBoolean(path+".foreground?");
            if(hasForeground)
                style.setForeground(PreferenceConverter.getDefaultColor(store, path+".foreground"));
            else
                style.setForeground(null);
            
            boolean hasBackground = store.getDefaultBoolean(path+".background?");
            if(hasBackground)
                style.setBackground(PreferenceConverter.getDefaultColor(store, path+".background"));
            else
                style.setBackground(null);
        }
    }

    public static void storeAsDefault(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            store.setDefault(path+".italic", style.isItalic());
            store.setDefault(path+".bold", style.isBold());
            
            store.setDefault(path+".foreground?", style.hasForeground());
            if(style.hasForeground())
                PreferenceConverter.setDefault(store, path+".foreground", style.getForegroundOrDefault());
            
            store.setDefault(path+".background?", style.hasBackground());
            if(style.hasBackground())
                PreferenceConverter.setDefault(store, path+".background", style.getBackgroundOrDefault());
        }
    }
    
    public static void store(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            store.setValue(path+".italic", style.isItalic());
            store.setValue(path+".bold", style.isBold());
            
            store.setValue(path+".foreground?", style.hasForeground());
            if(style.hasForeground())
                PreferenceConverter.setValue(store, path+".foreground", style.getForegroundOrDefault());
            
            store.setValue(path+".background?", style.hasBackground());
            if(style.hasBackground())
                PreferenceConverter.setValue(store, path+".background", style.getBackgroundOrDefault());
        }
    }

}
