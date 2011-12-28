package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.core.resources.IMarker;
import org.technbolts.eclipse.util.MarkData;

public class Marks {
    public static final String ERROR_CODE = "errorCode";

    public static final int MultipleMatchingSteps = 1;
    public static final int NoMatchingStep = 2;
    
    public static final int InvalidNarrativePosition = 3;
    public static final int InvalidNarrativeSequence_multipleNarrative = 4;
    public static final int InvalidNarrativeSequence_multipleInOrderTo = 5;    
    public static final int InvalidNarrativeSequence_multipleAsA = 6;
    public static final int InvalidNarrativeSequence_multipleIWantTo = 7;
    public static final int InvalidNarrativeSequence_missingNarrative = 8;
    public static final int InvalidNarrativeSequence_missingInOrderTo = 9;
    public static final int InvalidNarrativeSequence_missingAsA = 10;
    public static final int InvalidNarrativeSequence_missingIWantTo = 11;

    
    public static MarkData putCode(MarkData markData, int errorCode) {
        return markData.attribute(ERROR_CODE, errorCode);
    }
    
    public static int getCode(IMarker marker) {
        return marker.getAttribute(ERROR_CODE, -1);
    }
}
