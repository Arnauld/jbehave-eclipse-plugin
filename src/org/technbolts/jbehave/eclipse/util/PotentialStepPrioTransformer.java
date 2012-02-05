package org.technbolts.jbehave.eclipse.util;

import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.PotentialStep;

import fj.F;

public final class PotentialStepPrioTransformer extends F<PotentialStep, Integer> {

    @Override
    public Integer f(PotentialStep pStep) {
        try {
            Integer prioValue = JBehaveProject.getValue(pStep.annotation.getMemberValuePairs(), "priority");
            return prioValue == null ? Integer.valueOf(0) : prioValue;
        } catch (JavaModelException e) {
            return Integer.valueOf(0);
        }
    }
}