package org.technbolts.eclipse.log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class LogUtils {

    public static Log getLog(IProject project) throws CoreException {
        IFolder folder = project.getFolder(".technbolts");
        IFile file = folder.getFile("jbehave-eclipse-plugin.log");
        
        //at this point, no resources have been created
        if (!project.exists()) 
            project.create(null);
        if (!project.isOpen()) 
            project.open(null);
        if (!folder.exists()) 
           folder.create(IResource.NONE, true, null);
        if (!file.exists()) {
           byte[] bytes = "".getBytes();
           InputStream source = new ByteArrayInputStream(bytes);
           file.create(source, IResource.NONE, null);
        }
        
        return new LogImpl(file);
    }
}
