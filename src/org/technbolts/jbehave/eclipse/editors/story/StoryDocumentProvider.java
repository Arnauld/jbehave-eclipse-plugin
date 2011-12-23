package org.technbolts.jbehave.eclipse.editors.story;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.technbolts.eclipse.util.ProjectAwareFastPartitioner;
import org.technbolts.eclipse.util.UIUtils;
import org.technbolts.jbehave.support.JBPartition;
import org.technbolts.util.Strings;

public class StoryDocumentProvider extends FileDocumentProvider {

    protected IDocument createDocument(Object element) throws CoreException {
        if (element instanceof FileEditorInput) {
            final IProject project = ((FileEditorInput) element).getFile().getProject();
            IDocument document = super.createDocument(element);
            if (document != null) {
                IDocumentPartitioner partitioner = createPartitioner(project);
                partitioner.connect(document);
                document.setDocumentPartitioner(partitioner);
            }
            return document;
        }
        // TODO: what if it is not a file?
        UIUtils.warn("Unsupported type", "Cannot open the following type: " + element.getClass());
        return null;
    }

    private ProjectAwareFastPartitioner createPartitioner(final IProject project) {
        List<String> names = new ArrayList<String>(JBPartition.names());
        names.add((String)TokenConstants.IGNORED.getData());
        return new ProjectAwareFastPartitioner(
                new StoryPartitionScanner(),
                Strings.toArray(names), project);
    }

}
