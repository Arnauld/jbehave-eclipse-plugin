package org.technbolts.eclipse.util;

import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.technbolts.util.New;

public class TextAttributeProvider {
    public static class Entry {
        public final String keyId;
        public final int style;
        public final RGB foregroundColor;
        public final RGB backgroundColor;
        public Entry(String keyId, RGB foregroundColor) {
            this(keyId, foregroundColor, SWT.NORMAL);
        }
        public Entry(String keyId, RGB foregroundColor, int style) {
            this(keyId, foregroundColor, null, style);
        }
        public Entry(String keyId, RGB foregroundColor, RGB backgroundColor, int style) {
            this.keyId = keyId;
            this.foregroundColor = foregroundColor;
            this.backgroundColor = backgroundColor;
            this.style = style;
        }
        @Override
        public int hashCode() {
            return keyId.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Entry other = (Entry) obj;
            return keyId.equals(other.keyId);
        }
        
    }
    
    private Map<Entry, TextAttribute> textAttributes = New.hashMap();
    private ColorManager colorManager;
    public TextAttributeProvider(ColorManager colorManager) {
        super();
        this.colorManager = colorManager;
    }
    
    public TextAttribute get(Entry key) {
        TextAttribute textAttribute = textAttributes.get(key);
        if(textAttribute==null) {
            Color fcolor = colorManager.getColor(key.foregroundColor);
            Color bcolor = colorManager.getColor(key.backgroundColor);
            textAttribute = new TextAttribute(fcolor, bcolor, key.style);
            textAttributes.put(key, textAttribute);
        }
        return textAttribute;
    }
}
