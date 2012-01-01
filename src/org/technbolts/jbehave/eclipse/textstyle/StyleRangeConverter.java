package org.technbolts.jbehave.eclipse.textstyle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.technbolts.eclipse.util.ColorManager;

public class StyleRangeConverter {

    private final ColorManager colorManager;

    public StyleRangeConverter(ColorManager colorManager) {
        super();
        this.colorManager = colorManager;
    }
    public StyleRange createStyleRange(TextStyle style, int offset, int length) {
        
        Color foreground = colorManager.getColor(style.getForegroundOrDefault());
        Color background = colorManager.getColor(style.getBackgroundOrDefault());
        int fontStyle = SWT.NORMAL;
        if(style.isBold())
            fontStyle |= SWT.BOLD;
        if(style.isItalic())
            fontStyle |= SWT.ITALIC;
        
        StyleRange styleRange = new StyleRange(offset, length, foreground, background, fontStyle);
        return styleRange;
    }
}
