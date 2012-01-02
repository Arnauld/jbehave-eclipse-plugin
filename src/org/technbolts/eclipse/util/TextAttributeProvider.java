package org.technbolts.eclipse.util;

import java.util.Map;
import java.util.Observable;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.technbolts.jbehave.eclipse.textstyle.TextStyle;
import org.technbolts.util.New;

public class TextAttributeProvider extends Observable {
    
    private Map<String, TextAttribute> textAttributes = New.hashMap();
    
    private ColorManager colorManager;
    private Map<String, TextStyle> themeMap;
    
    public TextAttributeProvider(ColorManager colorManager) {
        super();
        this.colorManager = colorManager;
    }
    
    public synchronized TextAttribute get(String key) {
        TextAttribute textAttribute = textAttributes.get(key);
        if(textAttribute==null) {
            TextStyle textStyle = themeMap.get(key);
            Color fcolor = colorManager.getColor(textStyle.getForegroundOrDefault());
            Color bcolor = colorManager.getColor(textStyle.getBackgroundOrDefault());
            int style = SWT.NORMAL;
            if(textStyle.isBold())
                style |= SWT.BOLD;
            if(textStyle.isItalic())
                style |= SWT.ITALIC;
            textAttribute = new TextAttribute(fcolor, bcolor, style);
            textAttributes.put(key, textAttribute);
        }
        return textAttribute;
    }

    public synchronized void changeTheme(TextStyle theme) {
        this.themeMap = theme.createMap();
        this.textAttributes.clear();
        setChanged();
        notifyObservers(theme);
    }
}
