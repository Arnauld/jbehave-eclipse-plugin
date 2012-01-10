package org.technbolts.eclipse.jdt;

import static org.technbolts.util.Bytes.areDifferents;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
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

public class MethodPerPackageFragmentRootCache<E> extends
        JavaVisitorAdapter<MethodPerPackageFragmentRootCache.Bucket<E>> {

    private static final long NULL_STAMP = IResource.NULL_STAMP;

    private final ConcurrentMap<String, Container<E>> content = New.concurrentHashMap();
    private F<IMethod, Option<E>> transform;
    private byte[] cachedFilterHash;

    public MethodPerPackageFragmentRootCache(F<IMethod, Option<E>> transform) {
        this.transform = transform;
    }

    public void rebuild(IProject project, Effect<JavaScanner<?>> initializer) throws JavaModelException {
        JavaScanner<Bucket<E>> javaScanner = new JavaScanner<Bucket<E>>(project, this);
        initializer.e(javaScanner);
        byte[] filterHash = javaScanner.getFilterHash();
        boolean filterChanged = hasFilterChanged(filterHash);
        Bucket<E> rootBucket = newRootBucket(filterChanged);
        javaScanner.traversePackageFragmentRoots(rootBucket);
        removeUnsed(rootBucket);
    }

    protected boolean hasFilterChanged(byte[] bytes) {
        if (bytes == null || cachedFilterHash == null || areDifferents(bytes, cachedFilterHash)) {
            cachedFilterHash = bytes;
            return true;
        }
        return false;
    }

    public Bucket<E> newRootBucket(boolean hasFilterHashChanged) {
        return new Bucket<E>(hasFilterHashChanged);
    }

    protected void removeUnsed(Bucket<E> rootBucket) {
        List<String> keys = New.arrayList(content.keySet());
        for (String used : rootBucket.traversedPackageFragmentRoots) {
            keys.remove(used);
        }
        for (String unused : keys) {
            content.remove(unused);
        }
    }

    /**
     * Class is public in order to be invoked. But all methods are private for internal use.
     */
    public static final class Bucket<E> {
        private ConcurrentLinkedQueue<String> traversedPackageFragmentRoots = New.concurrentLinkedQueue();
        private Container<E> container;
        private boolean filterChanged;

        private Bucket(boolean filterChanged) {
            this.filterChanged = filterChanged;
        }

        private Bucket(Container<E> container) {
            this.container = container;
        }

        public boolean hasFilterChanged() {
            return filterChanged;
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
        System.out.println("MethodPerPackageFragmentRootCache.visit(*** PackageFragment: "
                + packageFragment.getElementName() + ")");
        return super.visit(packageFragment, arg);
    }

    @Override
    public boolean visit(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        long timestamp = modificationStampOf(packageFragmentRoot);
        String key = keyOf(packageFragmentRoot);
        bucket.traversePackageFragmentRoot(key);

        Container<E> typedMethods = content.get(key);
        if (typedMethods == null) {
            Container<E> newTypedMethods = new Container<E>(timestamp);
            typedMethods = content.putIfAbsent(key, newTypedMethods);
            if (typedMethods == null)
                typedMethods = newTypedMethods;

            // not scanned yet
            return true;
        } else {
            if (typedMethods.hasChanged(timestamp) || bucket.hasFilterChanged()) {
                // clear and rescan
                typedMethods.setTimestamp(timestamp);
                typedMethods.clear();
                return true;
            }

            System.out.println("MethodPerPackageFragmentRootCache.visit()::no change detected on [" + key + "]");

            // nothing else to scan
            return false;
        }
    }

    @Override
    public Bucket<E> argumentFor(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        String key = keyOf(packageFragmentRoot);
        Container<E> container = content.get(key);
        return bucket.withContainer(container);
    }

    private static String keyOf(IPackageFragmentRoot elem) {
        // rely on underlying file
        IPath path = elem.getPath();
        if (path != null)
            return StringUtils.reverse(path.toString());
        else
            return elem.getElementName();
    }

    private static long modificationStampOf(IPackageFragmentRoot elem) {
        IResource resource = elem.getResource();
        if (resource != null) {
            long ts = resource.getModificationStamp();
            if (ts != NULL_STAMP) {
                return ts;
            }
        }

        // rely on underlying file
        IPath path = elem.getPath();
        if (path != null)
            return path.toFile().lastModified();

        return NULL_STAMP;
    }

    @Override
    public boolean visit(IMethod method, Bucket<E> bucket) {
        Option<E> typedMethod = transform.f(method);
        if (typedMethod.isSome()) {
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

        public void clear() {
            elements.clear();
        }

        public void add(E element) {
            elements.add(element);
        }
    }

}
