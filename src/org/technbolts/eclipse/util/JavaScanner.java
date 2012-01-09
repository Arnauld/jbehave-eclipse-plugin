package org.technbolts.eclipse.util;

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

public class JavaScanner {
    
    public static class Visitor {
        private static final boolean dump = true;
        
        public Visitor() {
        }
        
        public boolean visit(IPackageFragmentRoot packageFragmentRoot) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(packageFragmentRoot...:" + packageFragmentRoot.getElementName() + ")");
            return true;
        }
        public boolean visit(IPackageFragment packageFragment) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(packageFragment.......:" + packageFragment.getElementName() + ")");
            return true;
        }
        public boolean traverseCompilationUnit(IPackageFragment packageFragment) {
            return false;
        }
        public boolean traverseClassFile(IPackageFragment packageFragment) {
            return false;
        }

        public boolean visit(ICompilationUnit compilationUnit) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(compilationUnit.......:" + compilationUnit.getElementName() + ")");
            return true;
        }
        public boolean visit(IClassFile classFile) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(classFile.............:" + classFile.getElementName() + ")");
            return true;
        }
        public boolean visit(IType type) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(type..................:" + type.getElementName() + ")");
            return true;
        }
        public boolean visit(IMethod method) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(method................:" + method.getElementName() + ")");
            return true;
        }
        public boolean visit(IJavaElement element) {
            if(dump)
                System.out.println("JavaScanner.Visitor.visit(element...............:" + element.getElementName() + "): " + element.getClass());
            return true;
        }
    }
    
    private final IProject project;
    private final Visitor visitor;
    private F<String, Boolean> packageRootNameFilter = FJ.alwaysTrue();
    private F<String, Boolean> packageNameFilter = FJ.alwaysTrue();
    private F<String, Boolean> classNameFilter = FJ.alwaysTrue();
    
    public JavaScanner(IProject project, Visitor visitor) {
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

    
    public void traversePackageFragmentRoots() throws JavaModelException {
        IJavaProject javaProject = (IJavaProject) JavaCore.create(project);
        for(IPackageFragmentRoot packageFragmentRoot : javaProject.getAllPackageFragmentRoots()) {
            if(!packageRootNameFilter.f(packageFragmentRoot.getElementName()))
                continue;
            
            if(visitor.visit(packageFragmentRoot)) {
                traverse(packageFragmentRoot);
            }
        }
    }
    
    protected void traverse(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
        for(IJavaElement jElem : packageFragmentRoot.getChildren()) {
            if(jElem.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                IPackageFragment packageFragment = (IPackageFragment)jElem;
                
                if(!packageNameFilter.f(packageFragment.getElementName()))
                    continue;
                
                if(visitor.visit(packageFragment)) {
                    traverse(packageFragment);
                }
            }
            else {
                visitor.visit(jElem);
            }
        }
    }
    
    protected void traverse(IPackageFragment packageFragment) throws JavaModelException {
        boolean traverseCUnit = visitor.traverseCompilationUnit(packageFragment);
        boolean traverseClassFile = visitor.traverseClassFile(packageFragment);
        
        for(IJavaElement jElem : packageFragment.getChildren()) {
            switch(jElem.getElementType()) {
                case IJavaElement.COMPILATION_UNIT:
                    if(!traverseCUnit)
                        break;
                    
                    if(!classNameFilter.f(jElem.getElementName()))
                        continue;
                    
                    ICompilationUnit cunit = (ICompilationUnit)jElem;
                    if(visitor.visit(cunit)) {
                        traverse(cunit);
                    }
                    break;
                case IJavaElement.CLASS_FILE:
                    if(!traverseClassFile)
                        break;

                    if(!classNameFilter.f(jElem.getElementName()))
                        continue;

                    IClassFile classFile = (IClassFile)jElem;
                    if(visitor.visit(classFile)) {
                        traverse(classFile);
                    }
                    break;
                default:
                    visitor.visit(jElem);
            }
        }
    }
    
    protected void traverse(ICompilationUnit compilationUnit) throws JavaModelException {
        for(IJavaElement jElem : compilationUnit.getChildren()) {
            if(jElem.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)jElem;
                if(visitor.visit(type)) {
                    traverseMethods(type);
                }
            }
            else {
                visitor.visit(jElem);
            }
        }
    }
    
    protected void traverse(IClassFile classFile) throws JavaModelException {
        for(IJavaElement jElem : classFile.getChildren()) {
            if(jElem.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)jElem;
                if(visitor.visit(type)) {
                    traverseMethods(type);
                }
            }
            else {
                visitor.visit(jElem);
            }
        }
    }
    
    protected void traverseMethods(IType type) throws JavaModelException {
        for(IJavaElement jElem : type.getChildren()) {
            if(jElem.getElementType() == IJavaElement.METHOD) {
                IMethod method = (IMethod)jElem;
                visitor.visit(method);
            }
            else {
                visitor.visit(jElem);
            }
        }
    }
    
}
