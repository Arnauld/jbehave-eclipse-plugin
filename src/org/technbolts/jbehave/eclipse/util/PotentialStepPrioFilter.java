package org.technbolts.jbehave.eclipse.util;

import org.technbolts.jbehave.eclipse.PotentialStep;

import fj.F;

final class PotentialStepPrioFilter extends F<PotentialStep, Boolean> {
    private final int maxPrio;

    PotentialStepPrioFilter(int maxPrio) {
        this.maxPrio = maxPrio;
    }

    @Override
    public Boolean f(PotentialStep pStep) {
        return maxPrio == pStep.priority;
    }
}