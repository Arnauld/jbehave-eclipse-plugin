package org.technbolts.eclipse.jdt;

import static org.technbolts.jbehave.eclipse.Activator.logError;
import static org.technbolts.util.Bytes.areDifferents;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.util.New;
import org.technbolts.util.Visitor;

import fj.Effect;
import fj.F;
import fj.data.Option;

public class MethodPerPackageFragmentRootCache<E> extends
        JavaVisitorAdapter<MethodPerPackageFragmentRootCache.Bucket<E>> {

    private static Logger log = LoggerFactory.getLogger(MethodPerPackageFragmentRootCache.class);

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

    public void traverse(Visitor<E, E> visitor) {
        for (Container<E> container : content.values()) {
            container.traverse(visitor);
            if (visitor.isDone())
                return;
        }
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

    @Override
    public boolean visit(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        long timestamp = modificationStampOf(packageFragmentRoot);
        String key = keyOf(packageFragmentRoot);
        bucket.traversePackageFragmentRoot(key);

        Container<E> typedMethods = content.get(key);
        if (typedMethods == null) {
            try {
                Container<E> newTypedMethods;
                if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    newTypedMethods = new HierarchicalContainer<E>();
                } else {
                    newTypedMethods = new FlatContainer<E>(timestamp);
                }
                content.putIfAbsent(key, newTypedMethods);
            } catch (JavaModelException e) {
                logError("Failed to retrieve kind of package fragment root", e);
            }

            // not scanned yet
            return true;
        } else {
            if (typedMethods.hasChanged(timestamp) || bucket.hasFilterChanged()) {
                // clear and rescan
                typedMethods.setTimestamp(timestamp);
                typedMethods.clear();
                return true;
            }

            log.debug("No change detected on [" + pathOf(packageFragmentRoot) + "] ts: " + timestamp);

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

    @Override
    public boolean visit(IPackageFragment packageFragment, Bucket<E> bucket) {
        IResource resource = packageFragment.getResource();
        long modificationStamp = (resource == null) ? -1 : resource.getModificationStamp();
        log.debug("*** PackageFragment: " + packageFragment.getElementName() + ") " + modificationStamp);
        return super.visit(packageFragment, bucket);
    }

    @Override
    public boolean traverseClassFile(IPackageFragment packageFragment, Bucket<E> bucket) {
        return true;
    }

    @Override
    public boolean traverseCompilationUnit(IPackageFragment packageFragment, Bucket<E> bucket) {
        return true;
    }

    @Override
    public boolean visit(ICompilationUnit cunit, Bucket<E> bucket) {
        long timestamp = cunit.getResource().getModificationStamp();
        log.debug("ICompilationUnit: " + cunit.getElementName() + ") ts: ts: " + timestamp);

        Container<E> container = bucket.container.specializeFor(cunit);
        if (container.hasChanged(timestamp) || bucket.hasFilterChanged()) {
            // clear and rescan
            container.setTimestamp(timestamp);
            container.clear();
            return true;
        }

        log.debug("ICompilationUnit: no change detected on [" + pathOf(cunit) + "] ts: " + timestamp);

        return false;
    }

    @Override
    public Bucket<E> argumentFor(ICompilationUnit compilationUnit, Bucket<E> bucket) {
        return bucket.withContainer(bucket.container.specializeFor(compilationUnit));
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

    private static String keyOf(IPackageFragmentRoot elem) {
        // the key is reversed, because it is usually the end that most differs
        // beginning is usually the library path, so it is shared among almost all keys
        return StringUtils.reverse(pathOf(elem));
    }

    private static String pathOf(IJavaElement elem) {
        // rely on underlying file
        IPath path = elem.getPath();
        if (path != null) {
            return path.toString();
        } else
            return elem.getElementName();
    }

    private static long modificationStampOf(IJavaElement elem) {
        try {
            if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
                if (((IPackageFragmentRoot) elem).getKind() == IPackageFragmentRoot.K_SOURCE) {
                    // modification stamp does not reflect a modification deeper in hierarchy
                    // so one cannot rely on it for step modification in source file
                    // unless it an archive such as a jar...
                    return NULL_STAMP;
                }
            } else if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                // modification stamp does not reflect a modification deeper in hierarchy
                return NULL_STAMP;
            }

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
        } catch (JavaModelException e) {
            return NULL_STAMP;
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

    public interface Container<E> {
        void setTimestamp(long timestamp);

        void traverse(Visitor<E, E> visitor);

        boolean hasChanged(long timestamp);

        void clear();

        void add(E element);

        Container<E> specializeFor(ICompilationUnit cunit);
    }

    public static class HierarchicalContainer<E> implements Container<E> {
        private final ConcurrentMap<String, Container<E>> children = New.concurrentHashMap();

        public void setTimestamp(long timestamp) {
        }

        public boolean hasChanged(long timestamp) {
            return true;
        }

        public void clear() {
        }

        public void add(E element) {
            throw new IllegalStateException();
        }

        @Override
        public void traverse(Visitor<E, E> visitor) {
            for (Container<E> container : children.values()) {
                container.traverse(visitor);
                if (visitor.isDone())
                    return;
            }
        }

        public Container<E> specializeFor(ICompilationUnit cunit) {
            long modificationStamp = modificationStampOf(cunit);

            String path = cunit.getPath().toString();
            Container<E> container = children.get(path);
            if (container == null) {
                Container<E> newContainer = new FlatContainer<E>(modificationStamp);
                container = children.putIfAbsent(path, newContainer);
                if (container == null)
                    container = newContainer;
            }
            return container;
        }
    }

    public static class FlatContainer<E> implements Container<E> {
        private long timestamp;
        private ConcurrentLinkedQueue<E> elements = new ConcurrentLinkedQueue<E>();

        public FlatContainer(long timestamp) {
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

        @Override
        public void traverse(Visitor<E, E> visitor) {
            for (E element : elements) {
                visitor.visit(element);
                if (visitor.isDone())
                    return;
            }
        }

        public Container<E> specializeFor(ICompilationUnit cunit) {
            return this;
        }
    }

}
