package org.technbolts.jbehave.eclipse.util;

import static fj.data.List.iterableList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.eclipse.util.JDTUtils;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.util.HasHTMLComment;
import org.technbolts.util.Visitor;

import fj.Ord;

public class StepLocator {
    
    public interface Provider {
        StepLocator getStepLocator();
    }
    
    public static StepLocator getStepLocator(JBehaveProject project) {
        return new StepLocator(project);
    }
    
    private JBehaveProject project;
    private StepLocator(JBehaveProject project) {
        this.project = project;
    }
    
    public static class WeightedCandidateStep implements Comparable<WeightedCandidateStep>, HasHTMLComment {
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
        
        private String htmlComment;
        @Override
        public String getHTMLComment() {
            if(htmlComment==null) {
                try {
                    htmlComment = JDTUtils.getJavadocOf(potentialStep.method);
                } catch (Exception e) {
                    htmlComment = "No documentation found";
                }
            }
            return htmlComment;
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
            LocalizedStepSupport localizedStepSupport = project.getLocalizedStepSupport();
            final String searchedType = LineParser.stepType(localizedStepSupport, stepLine);
            final String stepEntry = LineParser.extractStepSentence(localizedStepSupport, stepLine);
            
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
                        //Activator.logInfo(">> Step (" + weight + ") rejected: " + candidate);
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
    
    /**
     * Returns the first {@link PotentialStep} found that match the step, ordered by priority.
     * Be careful that there can be several other {@link PotentialStep}s that fulfill the step too.
     * 
     * @param step
     * @return
     */
    public PotentialStep findFirstStep(final String step) {
        try {
            Visitor<PotentialStep, PotentialStep> matchingStepVisitor = new Visitor<PotentialStep, PotentialStep>() {
                @Override
                public void visit(PotentialStep candidate) {
                    boolean matches = candidate.matches(step);
                    if(matches) {
                        add(candidate);
                    }
                }
            };
            traverseSteps(matchingStepVisitor);
            return getFirstStepWithHighestPrio(matchingStepVisitor.getFounds());
        } catch (JavaModelException e) {
            Activator.logError("Failed to find candidates for step <" + step + ">", e);
        }
        return null;
    }

    /**
     * 
     * @param findOne
     * @return
     */
    PotentialStep getFirstStepWithHighestPrio(Iterable<PotentialStep> potentialSteps) {
        fj.data.List<Integer> collectedPrios = iterableList(potentialSteps).map(new PotentialStepPrioTransformer());
        if (collectedPrios.isEmpty()) {
            return null;
        }
        
        final int maxPrio = collectedPrios.maximum(Ord.intOrd);
        fj.data.List<PotentialStep> maxPrioSteps = iterableList(potentialSteps).filter(new PotentialStepPrioFilter(maxPrio));
        
        return maxPrioSteps.head();
    }
    
    public IJavaElement findMethod(final String step) {
        PotentialStep pStep = findFirstStep(step);
        if(pStep!=null)
            return pStep.method;
        else
            return null;
    }
    
    public IJavaElement findMethodByQualifiedName(final String qualifiedName) {
        try {
            Visitor<PotentialStep, PotentialStep> findOne = new Visitor<PotentialStep, PotentialStep>() {
                @Override
                public void visit(PotentialStep candidate) {
                    String qName = JDTUtils.formatQualifiedName(candidate.method);
                    if(qName.equals(qualifiedName)) {
                        add(candidate);
                        done();
                    }
                }
            };
            traverseSteps(findOne);
            PotentialStep first = findOne.getFirst();
            if(first==null)
                return null;
            return first.method;
        } catch (JavaModelException e) {
            Activator.logError("Failed to find candidates for method <" + qualifiedName + ">", e);
        }
        return null;
    }

    
    public void traverseSteps(Visitor<PotentialStep, ?> visitor) throws JavaModelException {
        project.traverseSteps(visitor);
    }

}
