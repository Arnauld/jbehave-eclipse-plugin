package org.technbolts.jbehave.eclipse.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
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
import org.technbolts.eclipse.util.JavaAnalyzer;
import org.technbolts.util.StringEnhancer;

@SuppressWarnings("restriction")
public class StepLocator {
    
    /*
     *  When '$who' clicks on the '$button_id' button
     *  
     *  When 'Bob' clicks on the 'login' button
     *  When 'Bob' clicks on the ...
     *  When 'Bo 
     * 
     */
    
    public List<PotentialStep> findCandidatesStartingWith(final String stepLine, IProject project) {
        
        try {
            final String searchedType = LineParser.stepType(stepLine);
            final String stepEntry = LineParser.extractStepSentence(stepLine);
            Visitor findOne = new Visitor() {
                @Override
                protected void visit(PotentialStep candidate) {
                    if(!candidate.isTypeEqualTo(searchedType))
                        return;
                    
                    if(StringUtils.startsWithIgnoreCase(candidate.stepPattern, stepEntry)) {
                        add(candidate);
                    }
                }
            };
            traverseSteps(project, findOne);
            return findOne.getFounds();
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public IJavaElement findMethod(final String step, IProject project) {
        try {
            Visitor findOne = new Visitor() {
                @Override
                protected void visit(PotentialStep candidate) {
                    if(candidate.matches(step)) {
                        add(candidate);
                        done();
                    }
                }
            };
            traverseSteps(project, findOne);
            PotentialStep found = findOne.getFirst();
            return found==null?null:found.method;
        } catch (JavaModelException e) {
            e.printStackTrace();
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
    
    public void traverseSteps(IProject project, Visitor visitor) throws JavaModelException {
        JavaAnalyzer analyzer = new JavaAnalyzer();
        analyzer.collectTypes(project);
        for(IMethod method : analyzer.getMethods()) {
            
            StepType stepType = null;
            for(IAnnotation annotation : method.getAnnotations()) {
                String elementName = annotation.getElementName();
                
                List<String> patterns = new ArrayList<String>();
                if(StringEnhancer.enhanceString(elementName).endsWithOneOf("Given", "When", "Then")) {
                    // TODO check import declaration matches org.jbehave...
                    stepType = StepType.valueOf(elementName.toUpperCase());
                    String stepPattern = getValue(annotation.getMemberValuePairs(), "value");
                    patterns.add(stepPattern);
                }
                else if(StringEnhancer.enhanceString(elementName).endsWithOneOf("Aliases")) {
                    // TODO check import declaration matches org.jbehave...
                    Object aliases = getValue(annotation.getMemberValuePairs(), "values");
                    if(aliases instanceof Object[]) {
                        for(Object o : (Object[])aliases) {
                            if(o instanceof String)
                                patterns.add((String)o);
                        }
                        if(!patterns.isEmpty() && stepType==null)
                            stepType = StepType.GIVEN;
                    }
                }
                else if(StringEnhancer.enhanceString(elementName).endsWithOneOf("Alias")) {
                    // TODO check import declaration matches org.jbehave...
                    String stepPattern = getValue(annotation.getMemberValuePairs(), "value");
                    patterns.add(stepPattern);
                    if(!patterns.isEmpty() && stepType==null)
                        stepType = StepType.GIVEN;
                }
                
                for(String stepPattern : patterns) {
                    if(stepPattern==null)
                        continue;
                    
                    visitor.visit(new PotentialStep(method, annotation, stepType, stepPattern));
                    if(visitor.isDone())
                        return;
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getValue(IMemberValuePair[] memberValuePairs, String key) {
        for(IMemberValuePair kv : memberValuePairs) {
            if(kv.getMemberName().equalsIgnoreCase(key))
                return (T)kv.getValue();
        }
        return null;
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
    
    public static class Visitor {
        private boolean isDone;
        private List<PotentialStep> founds = new ArrayList<StepLocator.PotentialStep>();
        boolean isDone () {
            return isDone;
        }
        public void done () {
            this.isDone = true;
        }
        public void add(PotentialStep found) {
            this.founds.add(found);
        }
        public PotentialStep getFirst() {
            return founds.isEmpty()?null:founds.get(0);
        }
        public List<PotentialStep> getFounds() {
            return founds;
        }
        protected void visit(PotentialStep candidate) {
        }
    }
    
    /**
     * Candidate Step, prevent name clash with jbehave thus one uses potential instead.
     */
    public static class PotentialStep {
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
            return getMatcher(stepType, stepPattern).matches(step);
        }
    }

}
