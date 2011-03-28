package org.technbolts.jbehave.eclipse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.steps.StepType;
import org.technbolts.eclipse.util.JavaAnalyzer;
import org.technbolts.util.ArrayUtils;
import org.technbolts.util.New;
import org.technbolts.util.ProcessGroup;
import org.technbolts.util.StringEnhancer;

public class JBehaveProject {
    private IProject project;
    //
    private boolean useCache = JBehaveProjectRegistry.AwareOfJDTChange;
    //
    private List<IType> cachedTypes;
    private List<PotentialStep> cachedSteps;
    
    public JBehaveProject(IProject project) {
        this.project = project;
    }
    
    public void notifyChanges (IJavaElementDelta delta) {
        Activator.logInfo("Invalidating jbehave project <"+project.getName()+"> " + delta);
        cachedTypes = null;
        cachedSteps = null;
    }
    
    public IProject getProject() {
        return project;
    }
    
    protected List<IType> getCachedTypes() throws JavaModelException {
        if(useCache) {
            List<IType> types = this.cachedTypes;
            if(types==null) {
                types = collectTypes();
                this.cachedTypes = types;
            }
            return types;
        }
        else {
            return collectTypes();
        }
    }

    private List<IType> collectTypes() throws JavaModelException {
        JavaAnalyzer analyzer = new JavaAnalyzer();
        analyzer.collectTypes(project);
        return analyzer.getTypes();
    }
    
    public List<IMethod> getMethods() throws JavaModelException {
        List<IType> projectTypes = getCachedTypes();
        return JavaAnalyzer.getMethods(projectTypes);
    }

    public List<IType> getTypes() throws JavaModelException {
        return ArrayUtils.copyOf(getCachedTypes());
    }
    
    public void traverseSteps(Visitor visitor) throws JavaModelException {
        Activator.logInfo("JBehaveProject:Traversing Steps");
        for(PotentialStep step:getCachedSteps()) {
            visitor.visit(step);
            if(visitor.isDone())
                return;
        }
        Activator.logInfo("JBehaveProject:Steps traversed");
    }
    
    public List<PotentialStep> getCachedSteps () throws JavaModelException {
        List<PotentialStep> steps = cachedSteps;
        if(useCache && steps!=null)
            return steps;
        
        Activator.logInfo("JBehaveProject:Building steps cache for project <" + project.getName() + ">");
        StepCollectorContext context = new StepCollectorContext();
        ProcessGroup<Void> group = Activator.getDefault().newProcessGroup();
        for(IType type : getCachedTypes())
            group.spawn(stepCollector(type, context));
        
        try {
            Activator.logInfo("JBehaveProject:Waiting for steps cache group termination");
            group.awaitTermination();
        } catch (InterruptedException e) {
            Activator.logError("JBehaveProject:Interrupted while building steps cache for project <" + project.getName() + ">", e);
        }
        
        Activator.logInfo("JBehaveProject:Steps cache group built " + context.debug.toString());
        cachedSteps = context.collected;
        return context.collected;
    }
    
    private class StepCollectorContext {
        private final StringBuilder debug = new StringBuilder ();
        public final List<PotentialStep> collected = New.arrayList();

        public void addAll(List<PotentialStep> steps) {
            synchronized (collected) {
                collected.addAll(steps);
            }
        }
        public StepCollectorContext debugNL(CharSequence message) {
            debug.append(message);
            debug.append('\n');
            return this;
        }
    }
    
    private static Callable<Void> stepCollector(final IType type, final StepCollectorContext context) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                System.out.println("JBehaveProject:stepCollector(...).new Callable<Void>() {...}.call(" + type.getElementName() + ")");
                StringBuilder debug = new StringBuilder ("Analysis of type: " + type);
                for(IMethod method : JavaAnalyzer.getMethods(type)) {
                    List<PotentialStep> methodSteps = extractMethodSteps(debug, method);
                    context.addAll(methodSteps);
                }
                context.debugNL(debug);
                return null;
            }
        };
    }
    private static List<PotentialStep> extractMethodSteps(StringBuilder debug, IMethod method) throws JavaModelException {
        List<PotentialStep> steps = New.arrayList();
        
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

            if(!patterns.isEmpty()) {
                debug.append("StepLocator analysing method: " + method +"\n  found: " + patterns +"\n");
                for(String stepPattern : patterns) {
                    if(stepPattern==null)
                        continue;
                    steps.add(new PotentialStep(method, annotation, stepType, stepPattern));
                }
            }
        }

        return steps;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getValue(IMemberValuePair[] memberValuePairs, String key) {
        for(IMemberValuePair kv : memberValuePairs) {
            if(kv.getMemberName().equalsIgnoreCase(key))
                return (T)kv.getValue();
        }
        return null;
    }
}
