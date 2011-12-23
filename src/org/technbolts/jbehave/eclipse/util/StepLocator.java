package org.technbolts.jbehave.eclipse.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProjectRegistry;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.util.Visitor;

public class StepLocator {
    
    public static StepLocator getStepLocator(IProject project) {
        return new StepLocator(project);
    }
    
    private IProject project;
    private StepLocator(IProject project) {
        this.project = project;
    }
    
    public static class WeightedCandidateStep implements Comparable<WeightedCandidateStep> {
        public final PotentialStep potentialStep;
        public final float weight;
        public WeightedCandidateStep(PotentialStep potentialStep, float weight) {
            super();
            this.potentialStep = potentialStep;
            this.weight = weight;
        }
        @Override
        public int compareTo(WeightedCandidateStep o) {
            return (weight>o.weight)?1:-1;
        }
    }
    
    static boolean findCandidatesCheckStepType = true;
    
    /*
     *  When '$who' clicks on the '$button_id' button
     *  
     *  When 'Bob' clicks on the 'login' button
     *  When 'Bob' clicks on the ...
     *  When 'Bo 
     * 
     */
    public Iterable<WeightedCandidateStep> findCandidatesStartingWith(final String stepLine) {
        
        try {
            final String searchedType = LineParser.stepType(stepLine);
            final String stepEntry = LineParser.extractStepSentence(stepLine);
            
            Activator.logInfo("StepLocator.findCandidatesStartingWith(" + stepLine + "):" + searchedType + "//" + stepEntry + "<<");
            
            Visitor<PotentialStep, WeightedCandidateStep> findOne = new Visitor<PotentialStep, WeightedCandidateStep>() {
                @Override
                public void visit(PotentialStep candidate) {
                    boolean sameType = candidate.isTypeEqualTo(searchedType);
                    if(findCandidatesCheckStepType && !sameType) {
                        return;
                    }
                    
                    if(StringUtils.isBlank(stepEntry) && sameType) {
                        add(new WeightedCandidateStep(candidate, 0.1f));
                        return;
                    }
                    
                    float weight = candidate.weightOf(stepEntry);
                    if(weight>0) {
                        add(new WeightedCandidateStep(candidate, weight));
                    }
                    else {
                        Activator.logInfo(">> Step (" + weight + ") rejected: " + candidate);
                    }
                }
            };
            traverseSteps(findOne);
            return findOne.getFounds();
        } catch (JavaModelException e) {
            e.printStackTrace();
            Activator.logError("Failed to find candidates for step <" + stepLine + ">", e);
        }
        return null;
    }
    
    public IJavaElement findMethod(final String step) {
        try {
            Visitor<PotentialStep, IMethod> findOne = new Visitor<PotentialStep, IMethod>() {
                @Override
                public void visit(PotentialStep candidate) {
                    if(candidate.matches(step)) {
                        add(candidate.method);
                        done();
                    }
                }
            };
            traverseSteps(findOne);
            return findOne.getFirst();
        } catch (JavaModelException e) {
            Activator.logError("Failed to find candidates for step <" + step + ">", e);
        }
        return null;
    }
    
    public void traverseSteps(Visitor<PotentialStep, ?> visitor) throws JavaModelException {
        JBehaveProjectRegistry.get().getProject(project).traverseSteps(visitor);
    }
}
