package org.technbolts.eclipse.util;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class JDTUtils {

    public static String getJavadocOf(IMember member) throws JavaModelException, BadLocationException {
        String javadoc = getRawJavadocOf(member);
        javadoc = javadoc.replace('*', ' ');
        javadoc = javadoc.replace('/', ' ');
        javadoc = javadoc.replace('\n', ' ');
        javadoc = javadoc.replace("@", "\n@");  //$NON-NLS-1$//$NON-NLS-2$
        javadoc = javadoc.replace("  ", " "); //$NON-NLS-1$ //$NON-NLS-2$
        javadoc = javadoc.replace("   ", " "); //$NON-NLS-1$ //$NON-NLS-2$
        return javadoc;
    }
    
    public static String getRawJavadocOf(IMember member) throws JavaModelException, BadLocationException {
        String javadoc = "" ; //$NON-NLS-1$
        if(member.getSource()!= null) {
            IDocument source =  new Document(member.getSource());
            if(member.getJavadocRange() != null){
                javadoc = source.get(0,member.getJavadocRange().getLength());
            }
        }
        return javadoc;
    }
}
