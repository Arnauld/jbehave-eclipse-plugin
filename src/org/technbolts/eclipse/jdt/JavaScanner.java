package org.technbolts.eclipse.jdt;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.util.FJ;
import org.technbolts.util.StringMatcher;

import fj.F;

public class JavaScanner<T> {
    
    private final IProject project;
    private final JavaVisitor<T> visitor;
    private F<String, Boolean> packageRootNameFilter = FJ.alwaysTrue();
    private F<String, Boolean> packageNameFilter = FJ.alwaysTrue();
    private F<String, Boolean> classNameFilter = FJ.alwaysTrue();
    
    public JavaScanner(IProject project, JavaVisitor<T> visitor) {
        super();
        this.project = project;
        this.visitor = visitor;
    }
    
    public void setPackageNameFilter(StringMatcher packageNameMatcher) {
        setPackageNameFilter(packageNameMatcher.compile());
    }
    
    public void setPackageNameFilter(F<String, Boolean> packageNameFilter) {
        if(packageNameFilter==null)
            packageNameFilter = FJ.alwaysTrue();
        this.packageNameFilter = packageNameFilter;
    }
    
    public void setClassNameFilter(StringMatcher classNameMatcher) {
        setClassNameFilter(classNameMatcher.compile());
    }
    
    public void setClassNameFilter(F<String, Boolean> classNameFilter) {
        if(classNameFilter==null)
            classNameFilter = FJ.alwaysTrue();
        this.classNameFilter = classNameFilter;
    }

    public void setPackageRootNameFilter(StringMatcher packageRootNameMatcher) {
        setPackageRootNameFilter(packageRootNameMatcher.compile());
    }

    public void setPackageRootNameFilter(F<String, Boolean> packageRootNameFilter) {
        if(packageRootNameFilter==null)
            packageRootNameFilter = FJ.alwaysTrue();
        this.packageRootNameFilter = packageRootNameFilter;
    }

    
    public void traversePackageFragmentRoots(T argument) throws JavaModelException {
        IJavaProject javaProject = (IJavaProject) JavaCore.create(project);
        for(IPackageFragmentRoot packageFragmentRoot : javaProject.getAllPackageFragmentRoots()) {
            if(!packageRootNameFilter.f(packageFragmentRoot.getElementName()))
                continue;
            
            if(visitor.visit(packageFragmentRoot, argument)) {
                traverse(packageFragmentRoot, argument);
            }
        }
    }
    
    protected void traverse(IPackageFragmentRoot packageFragmentRoot, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(packageFragmentRoot, argument);
        
        for(IJavaElement jElem : packageFragmentRoot.getChildren()) {
            if(jElem.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                IPackageFragment packageFragment = (IPackageFragment)jElem;
                
                if(!packageNameFilter.f(packageFragment.getElementName()))
                    continue;
                
                if(visitor.visit(packageFragment, arg)) {
                    traverse(packageFragment, arg);
                }
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    
    protected void traverse(IPackageFragment packageFragment, T argument) throws JavaModelException {
        boolean traverseCUnit = visitor.traverseCompilationUnit(packageFragment, argument);
        boolean traverseClassFile = visitor.traverseClassFile(packageFragment, argument);
        T arg = visitor.argumentFor(packageFragment, argument);
        
        for(IJavaElement jElem : packageFragment.getChildren()) {
            switch(jElem.getElementType()) {
                case IJavaElement.COMPILATION_UNIT:
                    if(!traverseCUnit)
                        break;
                    
                    if(!classNameFilter.f(jElem.getElementName()))
                        continue;
                    
                    ICompilationUnit cunit = (ICompilationUnit)jElem;
                    if(visitor.visit(cunit, arg)) {
                        traverse(cunit, arg);
                    }
                    break;
                case IJavaElement.CLASS_FILE:
                    if(!traverseClassFile)
                        break;

                    if(!classNameFilter.f(jElem.getElementName()))
                        continue;

                    IClassFile classFile = (IClassFile)jElem;
                    if(visitor.visit(classFile, arg)) {
                        traverse(classFile, arg);
                    }
                    break;
                default:
                    visitor.visit(jElem, arg);
            }
        }
    }
    
    protected void traverse(ICompilationUnit compilationUnit, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(compilationUnit, argument);
        
        for(IJavaElement jElem : compilationUnit.getChildren()) {
            if(jElem.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)jElem;
                if(visitor.visit(type, arg)) {
                    traverseMethods(type, arg);
                }
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    
    protected void traverse(IClassFile classFile, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(classFile, argument);
        
        for(IJavaElement jElem : classFile.getChildren()) {
            if(jElem.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)jElem;
                if(visitor.visit(type, arg)) {
                    traverseMethods(type, arg);
                }
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    
    protected void traverseMethods(IType type, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(type, argument);
        
        for(IJavaElement jElem : type.getChildren()) {
            if(jElem.getElementType() == IJavaElement.METHOD) {
                IMethod method = (IMethod)jElem;
                visitor.visit(method, arg);
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    
}
