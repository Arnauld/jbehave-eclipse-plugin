package org.technbolts.eclipse.jdt.methodcache;

import static org.technbolts.util.Bytes.areDifferents;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.eclipse.jdt.JavaScanner;
import org.technbolts.eclipse.jdt.JavaVisitorAdapter;
import org.technbolts.util.Visitor;

import fj.Effect;
import fj.F;
import fj.data.Option;

public class MethodPerPackageFragmentRootCache<E> extends
        JavaVisitorAdapter<MethodPerPackageFragmentRootCache.Bucket<E>> {

    private static AtomicInteger buildTickGen = new AtomicInteger();
    
    private Logger log = LoggerFactory.getLogger(MethodPerPackageFragmentRootCache.class);

    private final HierarchicalContainer<E> content = new HierarchicalContainer<E>();
    private F<IMethod, Option<E>> transform;
    private byte[] cachedFilterHash;

    public MethodPerPackageFragmentRootCache(F<IMethod, Option<E>> transform) {
        this.transform = transform;
    }

    public void rebuild(IProject project, Effect<JavaScanner<?>> initializer) throws JavaModelException {
        JavaScanner<Bucket<E>> javaScanner = new JavaScanner<Bucket<E>>(project, this);
        initializer.e(javaScanner);
        
        int buildTick = buildTickGen.incrementAndGet();
        
        byte[] filterHash = javaScanner.getFilterHash();
        boolean filterChanged = hasFilterChanged(filterHash);
        Bucket<E> rootBucket = newRootBucket(buildTick, filterChanged);
        javaScanner.traversePackageFragmentRoots(rootBucket);
        removeUnsed(buildTick);
    }

    public void traverse(Visitor<E, E> visitor) {
        content.traverse(visitor);
    }

    protected boolean hasFilterChanged(byte[] bytes) {
        if (bytes == null || cachedFilterHash == null || areDifferents(bytes, cachedFilterHash)) {
            cachedFilterHash = bytes;
            return true;
        }
        return false;
    }

    public Bucket<E> newRootBucket(int buildTick, boolean hasFilterHashChanged) {
        return new Bucket<E>(buildTick, hasFilterHashChanged);
    }

    protected void removeUnsed(final int buildTick) {
        log.debug("Removing unsed container (e.g. element deleted, moved or renamed). This should be done asynchronously.");
        Job job = new Job("Victor the method cleaner (build #" + buildTick + ")") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                content.recursivelyRemoveBuildOlderThan(buildTick, monitor);
                return Status.OK_STATUS;
            }
        };
        job.schedule(); // start as soon as possible
    }

    @Override
    public boolean visit(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        Container<E> typedMethods = content.specializeFor(packageFragmentRoot);
        if(bucket.hasFilterChanged()) {
            typedMethods.resetForBuild(packageFragmentRoot, bucket.buildTick);
            return true;
        }
        else {
            return typedMethods.prepareForTraversal(packageFragmentRoot, bucket.buildTick);
        }
    }

    @Override
    public Bucket<E> argumentFor(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        return bucket.withContainer(content.specializeFor(packageFragmentRoot));
    }

    @Override
    public Bucket<E> argumentFor(IPackageFragment packageFragment, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(packageFragment);
        return bucket.withContainer(container);
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
        Container<E> typedMethods = content.specializeFor(cunit);
        if(bucket.hasFilterChanged()) {
            typedMethods.resetForBuild(cunit, bucket.buildTick);
            return true;
        }
        else {
            return typedMethods.prepareForTraversal(cunit, bucket.buildTick);
        }
    }

    @Override
    public Bucket<E> argumentFor(ICompilationUnit cunit, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(cunit);
        return bucket.withContainer(container);
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

    /**
     * Class is public in order to be invoked. But all methods are private for internal use.
     */
    public static final class Bucket<E> {
        private final Container<E> container;
        private final boolean filterChanged;
        private final int buildTick;

        private Bucket(int buildTick, boolean filterChanged) {
            this(buildTick, filterChanged, null);
        }

        private Bucket(int buildTick, boolean filterChanged, Container<E> container) {
            this.buildTick = buildTick;
            this.filterChanged = filterChanged;
            this.container = container;
        }

        public boolean hasFilterChanged() {
            return filterChanged;
        }

        private Container<E> getContainer() {
            return container;
        }

        private Bucket<E> withContainer(Container<E> container) {
            if (this.container == container)
                return this;
            return new Bucket<E>(buildTick, filterChanged, container);
        }
    }

}
