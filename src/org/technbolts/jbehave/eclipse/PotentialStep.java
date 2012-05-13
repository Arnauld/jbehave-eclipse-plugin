package org.technbolts.jbehave.eclipse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.StepType;
import org.technbolts.util.ParametrizedString;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * Candidate Step, prevent name clash with jbehave thus one uses potential instead.
 */
public class PotentialStep {
    private final LocalizedStepSupport localizedSupport;
    private final String parameterPrefix;
    public final IMethod method;
    public final IAnnotation annotation;
    public final StepType stepType;
    public final String stepPattern;
    private ParametrizedString parametrizedString;
    public final Integer priority;
    
    public PotentialStep(LocalizedStepSupport localizedSupport, String parameterPrefix, IMethod method, IAnnotation annotation, StepType stepType, String stepPattern, Integer priority) {
        super();
        this.localizedSupport = localizedSupport;
        this.parameterPrefix = parameterPrefix;
        this.method = method;
        this.annotation = annotation;
        this.stepType = stepType;
        this.stepPattern = stepPattern;
        this.priority = (priority==null)?Integer.valueOf(0):priority.intValue();
    }
    
    public float weightOf(String input) {
        return getParametrizedString().weightOf(input);
    }
    
    public ParametrizedString getParametrizedString() {
        if(parametrizedString==null)
            parametrizedString = new ParametrizedString(stepPattern, parameterPrefix);
        return parametrizedString;
    }
    
    public boolean hasVariable() {
        return getParametrizedString().getParameterCount()>0;
    }
    
    public boolean isTypeEqualTo(String searchedType) {
        return StringUtils.equalsIgnoreCase(searchedType, stepType.name());
    }
    
    public String fullStep() {
        return typeWord () + " " + stepPattern;
    }
    
    public String typeWord () {
        switch(stepType) {
            case WHEN: return localizedSupport.lWhen(false);
            case THEN: return localizedSupport.lThen(false);
            case GIVEN:
            default:
                return localizedSupport.lGiven(false);
        }
    }
    
    public boolean matches(String step) {
        return getMatcher(stepType, stepPattern).matches(step);
    }
    
    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(stepType).append("]").append(stepPattern).append(", ");
        if(method==null) {
            builder.append("n/a");
        }
        else {
            IType classFile = method.getDeclaringType();
            if(classFile!=null)
                builder.append(classFile.getElementName());
            else
                builder.append("<type-unknown>");
            builder.append('#').append(method.getElementName());
            
            try {
                Integer prio = JBehaveProject.getValue(annotation.getMemberValuePairs(), "priority");
                if (prio != null && prio.intValue() != 0) {
                    builder.append(", priority ").append(prio);
                }
            } catch (JavaModelException e) {
            }
        }
        return builder.toString();
    }
    
    private static RegexPrefixCapturingPatternParser stepParser = new RegexPrefixCapturingPatternParser();
    private static Cache<String, StepMatcher> matcherCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .weakKeys()
            .maximumSize(50)
            .expireAfterWrite(10*60, TimeUnit.SECONDS)
            .build(
                new CacheLoader<String, StepMatcher>() {
                  public StepMatcher load(String key) throws Exception {
                      int indexOf = key.indexOf('/');
                      StepType stepType = StepType.valueOf(key.substring(0, indexOf));
                      String stepPattern = key.substring(indexOf+1);
                      return stepParser.parseStep(stepType, stepPattern);
                  }
                });
    
    public static StepMatcher getMatcher(StepType stepType, String stepPattern) {
        try {
            String key = stepType.name()+"/"+stepPattern;
            return matcherCache.get(key);
        } catch (ExecutionException e) {
            // rely on parse
            return stepParser.parseStep(stepType, stepPattern);
        }
    }

}