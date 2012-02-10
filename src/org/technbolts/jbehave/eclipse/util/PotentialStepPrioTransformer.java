package org.technbolts.jbehave.eclipse.util;

import org.technbolts.jbehave.eclipse.PotentialStep;

import fj.F;

public final class PotentialStepPrioTransformer extends F<PotentialStep, Integer> {

    @Override
    public Integer f(PotentialStep pStep) {
        return pStep.priority;
    }
}