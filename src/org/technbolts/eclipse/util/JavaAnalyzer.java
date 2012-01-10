package org.technbolts.eclipse.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.technbolts.eclipse.jdt.JavaScanner;
import org.technbolts.eclipse.jdt.MethodPerPackageFragmentRootCache;
import org.technbolts.util.FJ;
import org.technbolts.util.StringMatcher;

import fj.Effect;

public class JavaAnalyzer {

    public static boolean isPackageFragmentRootOfSource(IJavaElement elem) throws JavaModelException {
        if (elem instanceof IPackageFragmentRoot) {
            return ((IPackageFragmentRoot) elem).getKind() == IPackageFragmentRoot.K_SOURCE;
        }
        return false;
    }

    private List<IType> collectedTypes = new ArrayList<IType>();

    public List<IType> getTypes() {
        return collectedTypes;
    }

    private static boolean fullScanDone = false;
    private static MethodPerPackageFragmentRootCache<IMethod> methodPerPackageFragmentRootCache = 
            new MethodPerPackageFragmentRootCache<IMethod>(FJ.<IMethod>identityOption());


    public void collectTypes(IProject project) throws JavaModelException {
        IJavaProject javaProject = (IJavaProject) JavaCore.create(project);
        for (IJavaElement jElem : javaProject.getChildren()) {
            if (isPackageFragmentRootOfSource(jElem)) {
                // sources are subject to change, let's rescan them entirely for
                // now
                collectTypes((IPackageFragmentRoot) jElem);
            }
        }

        if (fullScanDone)
            return;
        fullScanDone = false;//true;

        System.out.println("JavaAnalyzer.collectTypes() >>> Scanning all package fragment roots<<<");
        final StringMatcher packageNameMatcher = new StringMatcher().addGlobExcludes(//
                "apple.*", "com.apple.*", "quicktime.*", //
                "sun.*", "com.sun.*", "sunw.*", //
                "java.*", "javax.*", //
                "com.oracle.*", //
                "org.eclipse.*", //
                "com.google.common*", //
                "junit*", "org.junit*", //
                "org.omg.*", "org.xml.*", "org.w3c.*", "org.ietf*", "org.relaxng.*", "org.jcp.*", //
                "org.codehaus.plexus*", //
                "fj*", //
                "org.xmlpull.*", "com.thoughtworks.xstream*", "com.thoughtworks.paranamer*", // xstream
                "org.hamcrest*", "org.mockito*", "org.objenesis*", // mockito
                "org.apache.*", //
                "org.jbehave.*", //
                "freemarker*"
                );
        final StringMatcher classNameMatcher = new StringMatcher();//.addGlobIncludes("*Steps");
        final StringMatcher packageRootNameMatcher = new StringMatcher();//.addGlobIncludes("technbolts-*.jar");
        
        methodPerPackageFragmentRootCache.rebuild(project, new Effect<JavaScanner<?>>() {
            @Override
            public void e(JavaScanner<?> scanner) {
                scanner.setPackageRootNameFilter(packageRootNameMatcher);
                scanner.setPackageNameFilter(packageNameMatcher);
                scanner.setClassNameFilter(classNameMatcher);
            }
        });
    }
    

    public void collectTypes(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
        for (IJavaElement jElem : packageFragmentRoot.getChildren()) {
            if (jElem instanceof IPackageFragment) {
                collectTypes((IPackageFragment) jElem);
            }
        }
    }

    public void collectTypes(IPackageFragment packageFragment) throws JavaModelException {
        for (IJavaElement jElem : packageFragment.getChildren()) {
            if (jElem instanceof ICompilationUnit) {
                collectTypes((ICompilationUnit) jElem);
            }
        }
    }

    public void collectTypes(ICompilationUnit compilationUnit) throws JavaModelException {
        for (IJavaElement jElem : compilationUnit.getChildren()) {
            if (jElem instanceof IType) {
                collectedTypes.add((IType) jElem);
            }
        }
    }

    public List<IMethod> getMethods() throws JavaModelException {
        return getMethods(getTypes());
    }

    public static List<IMethod> getMethods(List<IType> types) throws JavaModelException {
        List<IMethod> methods = new ArrayList<IMethod>();
        for (IType type : types) {
            for (IJavaElement jElem : type.getChildren()) {
                if (jElem instanceof IMethod)
                    methods.add((IMethod) jElem);
            }
        }
        return methods;
    }

    public static List<IMethod> getMethods(IType type) throws JavaModelException {
        List<IMethod> methods = new ArrayList<IMethod>();
        for (IJavaElement jElem : type.getChildren()) {
            if (jElem instanceof IMethod)
                methods.add((IMethod) jElem);
        }
        return methods;
    }
}
