package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.core.resources.IMarker;
import org.technbolts.eclipse.util.MarkData;

public class Marks {
    public static final String ERROR_CODE = "errorCode";

    public static final int MultipleMatchingSteps = 1;
    public static final int NoMatchingStep = 2;
    
    public static MarkData putCode(MarkData markData, int errorCode) {
        return markData.attribute(ERROR_CODE, errorCode);
    }
    
    public static int getCode(IMarker marker) {
        return marker.getAttribute(ERROR_CODE, -1);
    }
}
