package org.technbolts.jbehave.eclipse.util;

import static fj.data.List.iterableList;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.util.JDTUtils;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.util.Visitor;

import fj.Ord;

public class StepLocator {
    
    private static Logger log = LoggerFactory.getLogger(StepLocator.class);
    
    private JBehaveProject project;
    public StepLocator(JBehaveProject project) {
        this.project = project;
    }
    
    static boolean findCandidatesCheckStepType = true;
    
    /**
     *  When '$who' clicks on the '$button_id' button
     *  
     *  When 'Bob' clicks on the 'login' button
     *  When 'Bob' clicks on the ...
     *  When 'Bo 
     * 
     */
    public Iterable<WeightedCandidateStep> findCandidatesStartingWith(final String stepLine) {
        log.debug("Attempt to find candidates starting with <{}>", stepLine);
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
            ConcurrentLinkedQueue<WeightedCandidateStep> founds = findOne.getFounds();
            log.debug("Candidates starting with <{}> found: #{}", stepLine, founds.size());
            return founds;
        } catch (JavaModelException e) {
            log.error("Failed to find candidates for step <" + stepLine + ">", e);
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
        log.debug("Attempt to find the first step matching <{}>", step);

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
            PotentialStep found = getFirstStepWithHighestPrio(matchingStepVisitor.getFounds());
            if(found==null) {
                log.debug("No candidate found matching <{}>", step);
                return null;
            }
            else {
                log.debug("First candidate matching <{}> found: <{}>", step, found.stepPattern);
                return found;
            }
        } catch (JavaModelException e) {
            log.error("Failed to find candidates for step <" + step + ">", e);
            Activator.logError("Failed to find candidates for step <" + step + ">", e);
        }
        return null;
    }

    /**
     * 
     * @param findOne
     * @return
     */
    private PotentialStep getFirstStepWithHighestPrio(Iterable<PotentialStep> potentialSteps) {
        fj.data.List<Integer> collectedPrios = iterableList(potentialSteps).map(new PotentialStepPrioTransformer());
        if (collectedPrios.isEmpty()) {
            return null;
        }
        
        final int maxPrio = collectedPrios.maximum(Ord.intOrd);
        fj.data.List<PotentialStep> maxPrioSteps = iterableList(potentialSteps).filter(new PotentialStepPrioFilter(maxPrio));
        
        return maxPrioSteps.head();
    }
    
    public IJavaElement findMethod(final String step) {
        log.debug("Attempt to find method for <{}>", step);
        PotentialStep pStep = findFirstStep(step);
        if(pStep!=null) {
            log.debug("Method found for <{}>: <{}>", step, pStep.method);
            return pStep.method;
        }
        else {
            log.debug("No method found for <{}>", step);
            return null;
        }
    }
    
    public IJavaElement findMethodByQualifiedName(final String qualifiedName) {
        log.debug("Attempt to find method using its qualified name <{}>", qualifiedName);
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
            if(first==null) {
                log.debug("No method found using its qualified name <{}>", qualifiedName);
                return null;
            }
            log.debug("Found method using its qualified name <{}>, got: {}", qualifiedName, first.method);
            return first.method;
        } catch (JavaModelException e) {
            Activator.logError("Failed to find candidates for method <" + qualifiedName + ">", e);
        }
        return null;
    }

    
    public void traverseSteps(Visitor<PotentialStep, ?> visitor) throws JavaModelException {
        log.debug("Traversing steps with: {}", visitor.getClass());
        project.traverseSteps(visitor);
    }

}
