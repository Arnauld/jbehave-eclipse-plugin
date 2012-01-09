package org.technbolts.eclipse.jdt;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.util.New;

import fj.Effect;
import fj.F;
import fj.data.Option;

public class MethodPerPackageFragmentRootCache<E> extends JavaVisitorAdapter<MethodPerPackageFragmentRootCache.Bucket<E>> {

    private static final long NULL_STAMP = IResource.NULL_STAMP;
    
    private final ConcurrentMap<String, Container<E>> content = New.concurrentHashMap();
    private F<IMethod, Option<E>> transform;
    
    public MethodPerPackageFragmentRootCache(F<IMethod, Option<E>> transform) {
        this.transform = transform;
    }
    
    public void rebuild(IProject project, Effect<JavaScanner<?>> initializer) throws JavaModelException {
        JavaScanner<Bucket<E>> javaScanner = new JavaScanner<Bucket<E>>(project, this);
        initializer.e(javaScanner);
        Bucket<E> rootBucket = newRootBucket();
        javaScanner.traversePackageFragmentRoots(rootBucket);
        removeUnsed(rootBucket);
    }
    
    public Bucket<E> newRootBucket () {
        return new Bucket<E>();
    }
    
    protected void removeUnsed(Bucket<E> rootBucket) {
        List<String> keys = New.arrayList(content.keySet());
        for(String used : rootBucket.traversedPackageFragmentRoots) {
            keys.remove(used);
        }
        for(String unused : keys) {
            content.remove(unused);
        }
    }
    
    /**
     * Class is public in order to be invoked. 
     * But all methods are private for internal use.
     */
    public static final class Bucket<E> {
        private ConcurrentLinkedQueue<String> traversedPackageFragmentRoots = New.concurrentLinkedQueue();
        private Container<E> container;
        private Bucket() {
        }
        private Bucket(Container<E> container) {
            this.container = container;
        }
        private void traversePackageFragmentRoot(String packageFragmentRoot) {
            traversedPackageFragmentRoots.add(packageFragmentRoot);
        }
        private Container<E> getContainer() {
            return container;
        }
        private Bucket<E> withContainer(Container<E> container) {
            return new Bucket<E>(container);
        }
    }
    
    @Override
    public boolean traverseClassFile(IPackageFragment packageFragment, Bucket<E> arg) {
        return true;
    }
    
    @Override
    public boolean traverseCompilationUnit(IPackageFragment packageFragment, Bucket<E> arg) {
        return true;
    }
    
    @Override
    public boolean visit(ICompilationUnit cunit, Bucket<E> arg) {
        System.out.println("MethodPerPackageFragmentRootCache.visit(ICompilationUnit: " + cunit.getElementName() + ")");
        return super.visit(cunit, arg);
    }
    
    @Override
    public boolean visit(IClassFile classFile, Bucket<E> arg) {
        System.out.println("MethodPerPackageFragmentRootCache.visit(ClassFile: " + classFile.getElementName() + ")");
        return super.visit(classFile, arg);
    }
    
    @Override
    public boolean visit(IPackageFragment packageFragment, Bucket<E> arg) {
        System.out.println("MethodPerPackageFragmentRootCache.visit(*** PackageFragment: " + packageFragment.getElementName() + ")");
        return super.visit(packageFragment, arg);
    }
    
    @Override
    public boolean visit(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        long timestamp = modificationStampOf(packageFragmentRoot);
        String elementName = packageFragmentRoot.getElementName();
        bucket.traversePackageFragmentRoot(elementName);
        
        Container<E> typedMethods = content.get(elementName);
        if(typedMethods==null) {
            Container<E> newTypedMethods = new Container<E>(timestamp);
            typedMethods = content.putIfAbsent(elementName, newTypedMethods);
            if(typedMethods==null)
                typedMethods = newTypedMethods;
            
            // not scanned yet
            return true;
        }
        else {
            if(typedMethods.hasChanged(timestamp)) {
                // clear and rescan
                typedMethods.setTimestamp(timestamp);
                typedMethods.clear();
                return true;
            }
            
            System.out.println("MethodPerPackageFragmentRootCache.visit()::no change detected on [" + elementName + "]");
            
            // nothing else to scan
            return false;
        }
    }
    
    @Override
    public Bucket<E> argumentFor(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        String elementName = packageFragmentRoot.getElementName();
        Container<E> container = content.get(elementName);
        return bucket.withContainer(container);
    }
    
    private static long modificationStampOf(IPackageFragmentRoot elem) {
        IResource resource = elem.getResource();
        if(resource!=null) {
            long ts = resource.getModificationStamp();
            if(ts!=NULL_STAMP) {
                return ts;
            }
        }
        
        // rely on underlying file
        IPath path = elem.getPath();
        if(path!=null)
            return path.toFile().lastModified();
        
        return NULL_STAMP;
    }
    
    @Override
    public boolean visit(IMethod method, Bucket<E> bucket) {
        Option<E> typedMethod = transform.f(method);
        if(typedMethod.isSome()) {
            Container<E> container = bucket.getContainer();
            container.add(typedMethod.some());
        }
        return false;
    }
    
    public static class Container<E> {
        private long timestamp;
        private ConcurrentLinkedQueue<E> elements = new ConcurrentLinkedQueue<E>();
        
        public Container(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public boolean hasChanged(long timestamp) {
            return this.timestamp != timestamp;
        }
        
        public void clear () {
            elements.clear();
        }
        
        public void add(E element) {
            elements.add(element);
        }
    }
    
}
