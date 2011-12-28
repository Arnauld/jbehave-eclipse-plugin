package org.technbolts.jbehave.eclipse.editors.story.quicksearch;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.technbolts.jbehave.eclipse.ImageIds;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.util.TextProvider;

public class QuickSearchStyledLabelProvider extends StyledCellLabelProvider implements TextProvider {
    
    private ImageRegistry imageRegistry;
    private boolean displayDecoration = false;
    
    /**
     * @param imageRegistry used to retrieve the label's image
     * @see ImageIds#STEP_GIVEN
     * @see ImageIds#STEP_WHEN
     * @see ImageIds#STEP_THEN
     */
    public QuickSearchStyledLabelProvider(ImageRegistry imageRegistry) {
        this.imageRegistry = imageRegistry;
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        if (element instanceof List) {
            cell.setText("/");
            return;
        }
        PotentialStep pStep = (PotentialStep) element;
        defineText(pStep, cell);
        defineImage(pStep, cell);
        
        super.update(cell);
    }
    
    public void setDisplayDecoration(boolean displayDecoration) {
        this.displayDecoration = displayDecoration;
    }
    
    public boolean isDisplayDecoration() {
        return displayDecoration;
    }
    
    public String textOf(Object element) {
        return ((PotentialStep)element).stepPattern;
    }

    private void defineText(PotentialStep pStep, ViewerCell cell) {
        StyledString styledString = new StyledString(textOf(pStep));
        
        if(displayDecoration) {
            String decoration = MessageFormat.format(" ({0}#{1})", new Object[] {
                     pStep.method.getParent().getElementName(),
                     pStep.method.getElementName()
                    }); //$NON-NLS-1$
            styledString.append(decoration, StyledString.QUALIFIER_STYLER);
        }

        cell.setText(styledString.toString());
        cell.setStyleRanges(styledString.getStyleRanges());
    }

    private void defineImage(PotentialStep pStep, ViewerCell cell) {
        String key = null;
        switch (pStep.stepType) {
            case GIVEN:
                key = ImageIds.STEP_GIVEN;
                break;
            case WHEN:
                key = ImageIds.STEP_WHEN;
                break;
            case THEN:
                key = ImageIds.STEP_THEN;
                break;
        }

        if (key != null) {
            cell.setImage(imageRegistry.get(key));
        }
    }
}
