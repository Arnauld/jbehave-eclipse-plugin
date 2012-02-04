package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.technbolts.eclipse.util.UIUtils;
import org.technbolts.util.New;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

    @Override
    public boolean hasResolutions(IMarker marker) {
        switch(Marks.getCode(marker)) {
            case MultipleMatchingSteps:
            case NoMatchingStep: {
                return true;
            }
        }
        return false;
    }

    @Override
    public IMarkerResolution[] getResolutions(IMarker marker) {
        List<IMarkerResolution> resolutions = New.arrayList();
        switch(Marks.getCode(marker)) {
            case MultipleMatchingSteps: {
                resolutions.add(new UhUhResolution());
                break;
            }
            case NoMatchingStep: {
                resolutions.add(new UhUhResolution());
                break;
            }
        }
        return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
    }
    
    static class UhUhResolution implements IMarkerResolution2 {

        @Override
        public String getLabel() {
            return "Uh Uh!";
        }

        @Override
        public String getDescription() {
            return "Say Uh Uh! in a nice popup!";
        }

        @Override
        public Image getImage() {
            return null;
        }
        
        @Override
        public void run(IMarker marker) {
            UIUtils.show("UhUh!", "Uh Uh! Every one!?!");
        }

    }
}
