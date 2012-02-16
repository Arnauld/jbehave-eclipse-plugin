package org.technbolts.jbehave.eclipse;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.technbolts.jbehave.support.JBKeyword;

public class KeywordImageRegistry {
    private ImageRegistry imageRegistry;
    public KeywordImageRegistry(ImageRegistry imageRegistry) {
        this.imageRegistry = imageRegistry;
    }
    
    public Image getImageFor(JBKeyword keyword) {
        switch(keyword) {
            case Given:
                return getImageRegistry().get(ImageIds.STEP_GIVEN);
            case When:
                return getImageRegistry().get(ImageIds.STEP_WHEN);
            case Then:
                return getImageRegistry().get(ImageIds.STEP_THEN);
            case And:
                return getImageRegistry().get(ImageIds.STEP_AND); 
            case GivenStories:
            case Meta:
            case MetaProperty:
                return getImageRegistry().get(ImageIds.META); 
            case AsA:
            case InOrderTo:
            case IWantTo:
            case Narrative:
                return getImageRegistry().get(ImageIds.NARRATIVE);
            case ExamplesTable:
            case ExamplesTableHeaderSeparator:
            case ExamplesTableIgnorableSeparator:
            case ExamplesTableRow:
            case ExamplesTableValueSeparator:
                return getImageRegistry().get(ImageIds.EXAMPLE_TABLE);
            case Scenario:
                return getImageRegistry().get(ImageIds.SCENARIO);
            case Ignorable:
                return getImageRegistry().get(ImageIds.IGNORABLE);
        }
        return null;
    }

    private ImageRegistry getImageRegistry() {
        return imageRegistry;
    }
}
