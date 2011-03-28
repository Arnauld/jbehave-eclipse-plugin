package org.technbolts.jbehave.eclipse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.util.StepLocator;

/**
 * Candidate Step, prevent name clash with jbehave thus one uses potential instead.
 */
public class PotentialStep {
    public final IMethod method;
    public final IAnnotation annotation;
    public final StepType stepType;
    public final String stepPattern;
    public PotentialStep(IMethod method, IAnnotation annotation, StepType stepType, String stepPattern) {
        super();
        this.method = method;
        this.annotation = annotation;
        this.stepType = stepType;
        this.stepPattern = stepPattern;
    }
    public boolean isTypeEqualTo(String searchedType) {
        return StringUtils.equalsIgnoreCase(searchedType, stepType.name());
    }
    public String fullStep() {
        return typeWord () + " " + stepPattern;
    }
    public String typeWord () {
        switch(stepType) {
            case WHEN: return "When";
            case THEN: return "Then";
            case GIVEN:
            default:
                return "Given";
        }
    }
    public boolean matches(String step) {
        return StepLocator.getMatcher(stepType, stepPattern).matches(step);
    }
    public String toString () {
        return method.getClassFile().getElementName()+"#"+method.getElementName();
    }
}