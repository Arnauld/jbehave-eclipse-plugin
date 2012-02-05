package org.technbolts.jbehave.eclipse.util;

import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.PotentialStep;

import fj.F;

final class PotentialStepPrioFilter extends F<PotentialStep, Boolean> {
    private final int maxPrio;

    PotentialStepPrioFilter(int maxPrio) {
        this.maxPrio = maxPrio;
    }

    @Override
    public Boolean f(PotentialStep pStep) {
        try {
            Integer prioValue = JBehaveProject.getValue(pStep.annotation.getMemberValuePairs(), "priority");
            prioValue = prioValue == null ? 0 : prioValue;
            return maxPrio == prioValue;
        } catch (JavaModelException e) {
            return maxPrio == 0;
        }
    }
}