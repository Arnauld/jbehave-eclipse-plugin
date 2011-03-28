package org.technbolts.jbehave.eclipse.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.JBehaveProjectRegistry;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.Visitor;

@SuppressWarnings("restriction")
public class StepLocator {
    
    public static StepLocator getStepLocator(IProject project) {
        return new StepLocator(project);
    }
    
    private IProject project;
    private StepLocator(IProject project) {
        this.project = project;
    }
    
    /*
     *  When '$who' clicks on the '$button_id' button
     *  
     *  When 'Bob' clicks on the 'login' button
     *  When 'Bob' clicks on the ...
     *  When 'Bo 
     * 
     */
    
    public List<PotentialStep> findCandidatesStartingWith(final String stepLine) {
        
        try {
            final String searchedType = LineParser.stepType(stepLine);
            final String stepEntry = LineParser.extractStepSentence(stepLine);
            Visitor findOne = new Visitor() {
                @Override
                public void visit(PotentialStep candidate) {
                    if(!candidate.isTypeEqualTo(searchedType))
                        return;
                    
                    if(StringUtils.startsWithIgnoreCase(candidate.stepPattern, stepEntry)) {
                        add(candidate);
                    }
                }
            };
            traverseSteps(findOne);
            return findOne.getFounds();
        } catch (JavaModelException e) {
            Activator.logError("Failed to find candidates for step <" + stepLine + ">", e);
        }
        return null;
    }
    
    public IJavaElement findMethod(final String step) {
        try {
            Visitor findOne = new Visitor() {
                @Override
                public void visit(PotentialStep candidate) {
                    if(candidate.matches(step)) {
                        add(candidate);
                        done();
                    }
                }
            };
            traverseSteps(findOne);
            PotentialStep found = findOne.getFirst();
            return found==null?null:found.method;
        } catch (JavaModelException e) {
            Activator.logError("Failed to find candidates for step <" + step + ">", e);
        }
        return null;
    }
    
    private static RegexPrefixCapturingPatternParser stepParser = new RegexPrefixCapturingPatternParser();
    private static Map<String, StepMatcher> matcherCache = new ConcurrentHashMap<String, StepMatcher>();
    public static StepMatcher getMatcher(StepType stepType, String stepPattern) {
        String key = stepType.name()+"/"+stepPattern;
        StepMatcher matcher = matcherCache.get(key);
        if(matcher==null) {
            matcher = stepParser.parseStep(stepType, stepPattern);
            matcherCache.put(key, matcher);
        }
        return matcher;
    }
    
    public void traverseSteps(Visitor visitor) throws JavaModelException {
        JBehaveProjectRegistry.get().getProject(project).traverseSteps(visitor);
    }

    @SuppressWarnings("unused")
    private ResolvedSourceMethod findMethodUsingSearch(String step, IProject project) {
            //TODO: this search can be tuned to search only once and return all the implementations.
            //Then match from memory.
            IJavaProject myJavaProject = (IJavaProject) JavaCore.create(project);
            SearchPattern pattern = SearchPattern.createPattern(
                    "*", 
                    IJavaSearchConstants.ANNOTATION_TYPE, 
                    IJavaSearchConstants.REFERENCES, 
                    SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE);
            IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {myJavaProject});
            final StepSearchRequestor requestor = new StepSearchRequestor(step);                                

            //search
            SearchEngine searchEngine = new SearchEngine();
            try {
                searchEngine.search(pattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, null);
            } catch (CoreException e) {
                throw new RuntimeException("Something went wrong when searching for Step", e);
            }
            
            return requestor.methodToJump;
    }

}
