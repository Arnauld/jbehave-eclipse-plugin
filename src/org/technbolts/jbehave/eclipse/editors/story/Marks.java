package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.technbolts.eclipse.util.MarkData;
import org.technbolts.jbehave.eclipse.PotentialStep;

public class Marks {
    public static final String ERROR_CODE = "errorCode";
    public static final String STEPS_HTML = "stepsHtml";

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

    public static MarkData putStepsAsHtml(MarkData mark, List<PotentialStep> candidates) {
        StringBuilder builder = new StringBuilder();
        builder.append("<ul>");
        for(PotentialStep pStep : candidates) {
            ICompilationUnit cu= (ICompilationUnit)pStep.method.getAncestor(IJavaElement.COMPILATION_UNIT);
            builder
                .append("<li>")
                .append("<b>")
                .append(StringEscapeUtils.escapeHtml(pStep.stepPattern))
                .append("</b>")
                .append(" (<code><a href=\"");
            if (cu != null) {
                builder.append(cu.getElementName()).append("#");
            }
            builder.append(pStep.method.getElementName());
            builder.append("\">");
            if (cu != null) {
                builder.append(cu.getElementName()).append("#");
            }
            builder
                .append(pStep.method.getElementName())
                .append("</a></code>)")
                .append("</li>");
        }
        builder.append("</ul>");
        return mark.attribute(STEPS_HTML, builder.toString());
    }
}
