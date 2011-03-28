package org.technbolts.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.technbolts.jbehave.eclipse.Activator;

public class ProjectAwareFastPartitioner extends FastPartitioner {
    IProject project;

    public ProjectAwareFastPartitioner(IPartitionTokenScanner scanner,
            String[] legalContentTypes, IProject project) {
        super(scanner, legalContentTypes);
        this.project = project;
    }

    public IProject getProject() {
        return project;
    }
    
    @Override
    public void connect(IDocument document, boolean delayInitialization) {
        super.connect(document, delayInitialization);
        printPartitions(document);
    }

    public void printPartitions(IDocument document)
    {
        StringBuffer buffer = new StringBuffer();

        ITypedRegion[] partitions = computePartitioning(0, document.getLength());
        for (int i = 0; i < partitions.length; i++)
        {
            try
            {
                buffer.append("Partition type: " 
                  + partitions[i].getType() 
                  + ", offset: " + partitions[i].getOffset()
                  + ", length: " + partitions[i].getLength());
                buffer.append("\n");
                buffer.append("Text:\n");
                buffer.append(document.get(partitions[i].getOffset(), 
                 partitions[i].getLength()));
                buffer.append("\n---------------------------\n\n\n");
            }
            catch (BadLocationException e)
            {
                Activator.logError("Ooops while printing partitions", e);
            }
        }
        
        Activator.logInfo(buffer.toString());
    }

}
