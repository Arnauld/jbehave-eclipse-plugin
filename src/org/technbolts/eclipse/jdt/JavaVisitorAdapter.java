package org.technbolts.eclipse.jdt;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

public class JavaVisitorAdapter<T> implements JavaVisitor<T> {

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.IPackageFragmentRoot, java.lang.Object)
     */
    @Override
    public boolean visit(IPackageFragmentRoot packageFragmentRoot, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#argumentFor(org.eclipse.jdt.core.IPackageFragmentRoot, java.lang.Object)
     */
    @Override
    public T argumentFor(IPackageFragmentRoot packageFragmentRoot, T arg) {
        return arg;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.IPackageFragment, java.lang.Object)
     */
    @Override
    public boolean visit(IPackageFragment packageFragment, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#argumentFor(org.eclipse.jdt.core.IPackageFragment, java.lang.Object)
     */
    @Override
    public T argumentFor(IPackageFragment packageFragment, T arg) {
        return arg;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#traverseCompilationUnit(org.eclipse.jdt.core.IPackageFragment, java.lang.Object)
     */
    @Override
    public boolean traverseCompilationUnit(IPackageFragment packageFragment, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#traverseClassFile(org.eclipse.jdt.core.IPackageFragment, java.lang.Object)
     */
    @Override
    public boolean traverseClassFile(IPackageFragment packageFragment, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.ICompilationUnit, java.lang.Object)
     */
    @Override
    public boolean visit(ICompilationUnit compilationUnit, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#argumentFor(org.eclipse.jdt.core.ICompilationUnit, java.lang.Object)
     */
    @Override
    public T argumentFor(ICompilationUnit compilationUnit, T arg) {
        return arg;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.IClassFile, java.lang.Object)
     */
    @Override
    public boolean visit(IClassFile classFile, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#argumentFor(org.eclipse.jdt.core.IClassFile, java.lang.Object)
     */
    @Override
    public T argumentFor(IClassFile classFile, T arg) {
        return arg;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.IType, java.lang.Object)
     */
    @Override
    public boolean visit(IType type, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#argumentFor(org.eclipse.jdt.core.IType, java.lang.Object)
     */
    @Override
    public T argumentFor(IType classFile, T arg) {
        return arg;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.IMethod, java.lang.Object)
     */
    @Override
    public boolean visit(IMethod method, T arg) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.technbolts.eclipse.jdt.JavaVisitor#visit(org.eclipse.jdt.core.IJavaElement, java.lang.Object)
     */
    @Override
    public boolean visit(IJavaElement element, T arg) {
        return false;
    }

    
}
