package org.technbolts.eclipse.jdt.methodcache;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.technbolts.util.Visitor;

public class FlatContainer<E> extends Container<E> {
    private ConcurrentLinkedQueue<E> elements = new ConcurrentLinkedQueue<E>();

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
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
    
    @Override
    public void recursivelyRemoveBuildOlderThan(int buildTick, IProgressMonitor monitor) {
    }

    @Override
    public Container<E> specializeFor(IPackageFragmentRoot pkgFragmentRoot) {
        return this;
    }

    @Override
    public Container<E> specializeFor(ICompilationUnit cunit) {
        return this;
    }

    @Override
    public Container<E> specializeFor(IPackageFragment pkgFragment) {
        return this;
    }
}