package org.technbolts.jbehave.eclipse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.PatternVariantBuilder;
import org.jbehave.core.steps.StepType;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.jdt.JavaScanner;
import org.technbolts.eclipse.jdt.methodcache.Container;
import org.technbolts.eclipse.jdt.methodcache.Containers;
import org.technbolts.eclipse.jdt.methodcache.MethodPerPackageFragmentRootCache;
import org.technbolts.jbehave.eclipse.preferences.ClassScannerPreferences;
import org.technbolts.jbehave.eclipse.preferences.ProjectPreferences;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.C2;
import org.technbolts.util.CharTree;
import org.technbolts.util.LocaleUtils;
import org.technbolts.util.ProcessGroup;
import org.technbolts.util.StringEnhancer;
import org.technbolts.util.Visitor;

import fj.Effect;

public class JBehaveProject {
    private static Logger log = LoggerFactory.getLogger(JBehaveProject.class);

    private IProject project;
    //
    private MethodPerPackageFragmentRootCache<PotentialStep> cache;

    private Locale storyLocale;

    private LocalizedKeywords localizedKeywords;
    
    private CharTree<JBKeyword> kwTree;
    //
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private ProjectPreferences projectPreferences;


	private static String plusSpace(String aString, boolean wantSpace) {
		return wantSpace ? aString + " " : aString;
	}
	
    public JBehaveProject(IProject project) {
        this.project = project;
        this.cache = new MethodPerPackageFragmentRootCache<PotentialStep>(newCallback());
        this.reloadProjectPreferences();
    }
    
    private void reloadProjectPreferences() {
        this.projectPreferences = new ProjectPreferences(getProject());
        try {
            projectPreferences.load();
        } catch (BackingStoreException e) {
            log.error("Failed to load project preferences", e);
        }
        storyLocale = LocaleUtils.createLocaleFromCode(projectPreferences.getStoryLanguage(), Locale.ENGLISH);
        localizedKeywords = new LocalizedKeywords(storyLocale);
        kwTree = createKeywordCharTree();
    }
    
    public Locale getLocale() {
        return storyLocale;
    }
    
    public CharTree<JBKeyword> sharedKeywordCharTree() {
        return kwTree;
    }
    
     public LocalizedKeywords getLocalizedKeywords() {
        return localizedKeywords;
    }
    
    public CharTree<JBKeyword> createKeywordCharTree() {
        LocalizedKeywords keywords = getLocalizedKeywords();
        CharTree<JBKeyword> cn = new CharTree<JBKeyword>('/', null);
        for(JBKeyword kw : JBKeyword.values()) {
            String asString = kw.asString(keywords);
            cn.push(asString, kw);
        }
        return cn;
    }
    
    public String lGiven(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.given(), withTrailingSpace);
    }
    public String lAnd(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.and(), withTrailingSpace);
    }
    public String lAsA(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.asA(), withTrailingSpace);
    }
    public String lExamplesTable(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.examplesTable(), withTrailingSpace);
    }
    public String lGivenStories(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.givenStories(), withTrailingSpace);
    }
    public String lIgnorable(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.ignorable(), withTrailingSpace);
    }
    public String lInOrderTo(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.inOrderTo(), withTrailingSpace);
    }
    public String lIWantTo(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.iWantTo(), withTrailingSpace);
    }
    public String lMeta(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.meta(), withTrailingSpace);
    }
    public String lNarrative(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.narrative(), withTrailingSpace);
    }
    public String lScenario(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.scenario(), withTrailingSpace);
    }
    public String lThen(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.then(), withTrailingSpace);
    }
    public String lWhen(boolean withTrailingSpace) {
        return plusSpace(localizedKeywords.when(), withTrailingSpace);
    }

    private C2<IMethod, Container<PotentialStep>> newCallback() {
        return new C2<IMethod, Container<PotentialStep>>() {
         public void op(IMethod method, Container<PotentialStep> container) {
             try {
                extractMethodSteps(method, container);
            } catch (JavaModelException e) {
                log.error("Failed to extract step from method <" + method + ">", e);
            }
         };
      };
    }
    
    private AtomicInteger comod = new AtomicInteger();
    private volatile int rebuildTick = -1;
    
    public void notifyChanges (IJavaElementDelta delta) {
        log.debug("Notify JDT change within project <"+project.getName()+"> " + delta);
        comod.incrementAndGet();
    }
    
    public IProject getProject() {
        return project;
    }
    
    public void traverseSteps(Visitor<PotentialStep, ?> visitor) throws JavaModelException {
        boolean rAcquired = true;
        rwLock.readLock().lock();
        try {
            int mod = comod.get();
            if(rebuildTick!=mod) {
                // promote lock
                rwLock.readLock().unlock();   // must unlock first to obtain writelock
                rAcquired = false;
                rwLock.writeLock().lock();
                try {
                    rebuildTick = mod;
                    rebuild();
                    
                    // Downgrade by acquiring read lock before releasing write lock
                    rwLock.readLock().lock();
                    rAcquired = true;
                }
                finally {
                    rwLock.writeLock().unlock(); // Unlock write, still hold read
                }
            }
            
            log.info("Traversing cache for project <"+project.getName()+">");
            cache.traverse(visitor);
        }
        finally {
            if(rAcquired)
                rwLock.readLock().unlock();
        }
    }
    
    protected void rebuild () {
        log.info("Re-building cache for project <"+project.getName()+">");

        ProcessGroup<Void> processGroup = Activator.getDefault().newProcessGroup();
        try {
            cache.rebuild(project, new Effect<JavaScanner<?>>() {
                @Override
                public void e(JavaScanner<?> scanner) {
                    try {
                        ClassScannerPreferences prefs = new ClassScannerPreferences(project);
                        prefs.load();
                        scanner.setFilterHash(prefs.calculateHash());
                        scanner.setPackageRootNameFilter(prefs.getPackageRootMatcher());
                        scanner.setPackageNameFilter(prefs.getPackageMatcher());
                        scanner.setClassNameFilter(prefs.getClassMatcher());
                    } catch (BackingStoreException e) {
                        log.error("Failed to load scanner preferences", e);
                    }
                }
            }, processGroup);
        } catch (JavaModelException e) {
            log.error("Error during cache rebuild", e);
            // invalidate cache
            comod.incrementAndGet();
        }
        
        try {
            processGroup.awaitTermination();
        } catch (InterruptedException e) {
            log.info("Java traversal interrupted");
            // invalidate cache
            comod.incrementAndGet();
        }
    }
    
    private void extractMethodSteps(IMethod method, Container<PotentialStep> container) throws JavaModelException {
        StepType stepType = null;
        for(IAnnotation annotation : method.getAnnotations()) {
            String elementName = annotation.getElementName();
            IMemberValuePair[] annotationAttributes = annotation.getMemberValuePairs();
            Integer priority = Integer.valueOf(0);
            
            List<String> patterns = new ArrayList<String>();
            if(StringEnhancer.enhanceString(elementName).endsWithOneOf("Given", "When", "Then")) {
                // TODO check import declaration matches org.jbehave...
                stepType = StepType.valueOf(elementName.toUpperCase());
                String stepPattern = getValue(annotationAttributes, "value");
                priority = getValue(annotationAttributes, "priority");
                PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
                for (String variant : b.allVariants()) {
                	patterns.add(variant);
                }
            }
            else if(StringEnhancer.enhanceString(elementName).endsWithOneOf("Aliases")) {
                // TODO check import declaration matches org.jbehave...
                Object aliases = getValue(annotationAttributes, "values");
                if(aliases instanceof Object[]) {
                    for(Object o : (Object[])aliases) {
                        if(o instanceof String) {
                        	PatternVariantBuilder b = new PatternVariantBuilder((String) o);
                        	for (String variant : b.allVariants()) {
                            	patterns.add(variant);
                            }       	
                        }
                    }
                    if(!patterns.isEmpty() && stepType==null)
                        stepType = StepType.GIVEN;
                }
            }
            else if(StringEnhancer.enhanceString(elementName).endsWithOneOf("Alias")) {
                // TODO check import declaration matches org.jbehave...
                String stepPattern = getValue(annotationAttributes, "value");
                PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
                for (String variant : b.allVariants()) {
                	patterns.add(variant);
                }
                
                if(!patterns.isEmpty() && stepType==null)
                    stepType = StepType.GIVEN;
            }

            if(!patterns.isEmpty()) {
                log.debug("StepLocator analysing method: " + Containers.pathOf(method) +" found: " + patterns);
                for(String stepPattern : patterns) {
                    if(stepPattern==null)
                        continue;
                    container.add(new PotentialStep(this, method, annotation, stepType, stepPattern, priority));
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getValue(IMemberValuePair[] memberValuePairs, String key) {
        for(IMemberValuePair kv : memberValuePairs) {
            if(kv.getMemberName().equalsIgnoreCase(key))
                return (T)kv.getValue();
        }
        return null;
    }

}
