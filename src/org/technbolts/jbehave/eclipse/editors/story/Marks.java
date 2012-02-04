package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.core.resources.IMarker;
import org.technbolts.eclipse.util.MarkData;

public class Marks {
    public static final String ERROR_CODE = "errorCode";

    public enum Code {
        Unknown(-1),
        MultipleMatchingSteps(1),
        MultipleMatchingSteps_PrioritySelection(12),
        NoMatchingStep(2),
        InvalidNarrativePosition(3),
        InvalidNarrativeSequence_multipleNarrative(4),
        InvalidNarrativeSequence_multipleInOrderTo(5),    
        InvalidNarrativeSequence_multipleAsA(6),
        InvalidNarrativeSequence_multipleIWantTo(7),
        InvalidNarrativeSequence_missingNarrative(8),
        InvalidNarrativeSequence_missingInOrderTo(9),
        InvalidNarrativeSequence_missingAsA(10),
        InvalidNarrativeSequence_missingIWantTo(11);
        
        private final int legacyCode;
        private Code(int legacyCode) {
            this.legacyCode = legacyCode;
        }
        public static Code lookup(int intCode, Code fallback) {
            for(Code c : values()) {
                if(c.legacyCode == intCode)
                    return c;
            }
            return fallback;
        }
    }

    
    public static MarkData putCode(MarkData markData, Code errorCode) {
        return markData.attribute(ERROR_CODE, errorCode.legacyCode);
    }
    
    public static Code getCode(IMarker marker) {
        return Code.lookup(marker.getAttribute(ERROR_CODE, -1), Code.Unknown);
    }
}
