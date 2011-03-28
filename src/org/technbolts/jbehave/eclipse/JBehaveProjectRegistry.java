package org.technbolts.jbehave.eclipse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class JBehaveProjectRegistry {
    
    static boolean AwareOfJDTChange = false;
    
    private static JBehaveProjectRegistry singleton = new JBehaveProjectRegistry();
    public static JBehaveProjectRegistry get() {
        singleton.registerListenerIfRequired();
        return singleton;
    }
    
    private ConcurrentHashMap<IProject, JBehaveProject> projectCache = new ConcurrentHashMap<IProject, JBehaveProject>();
    private AtomicBoolean listening = new AtomicBoolean();
    
    private void registerListenerIfRequired() {
        if(listening.compareAndSet(false, true)) {
            JavaCore.addElementChangedListener(new IElementChangedListener() {
                @Override
                public void elementChanged(ElementChangedEvent event) {
                    notifyChanges(event.getDelta());
                }
            });
        }
    }
    
    private static boolean hasAnnotationFlag(IJavaElementDelta delta) {
        int mask = IJavaElementDelta.F_ANNOTATIONS;
        return (delta.getFlags() & mask)==mask;
    }
    
    private void notifyChanges(IJavaElementDelta delta) {
        if(!hasAnnotationFlag(delta)) {
            for(IJavaElementDelta sub : delta.getAffectedChildren())
                notifyChanges(sub);
            return;
        }
        for(IJavaElementDelta sub : delta.getAnnotationDeltas()) {
            IProject project = extractProject(sub);
            if(project!=null)
                notifyProjectChanges(project, sub);
        }
    }
    
    private static IProject extractProject(IJavaElementDelta delta) {
        IJavaElement element = delta.getElement();
        if(element==null)
            return null;
        IJavaProject javaProject = element.getJavaProject();
        if(javaProject==null)
            return null;
        return javaProject.getProject();
    }
    
    protected void notifyProjectChanges(IProject project, IJavaElementDelta delta) {
        getProject(project).notifyChanges(delta);
    }
    
    public JBehaveProject getProject(IProject project) {
        JBehaveProject cache = projectCache.get(project);
        if(cache==null) {
            JBehaveProject newCache = new JBehaveProject (project);
            cache = projectCache.putIfAbsent(project, newCache);
            if(cache==null)
                cache = newCache;
        }
        return cache;
    }
    
}
