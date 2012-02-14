package org.technbolts.jbehave.eclipse.editors.story;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.jbehave.parser.StoryParser;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartVisitor;

public class StoryDocument extends Document {

    private volatile List<StoryPart> parts;
    private JBehaveProject jbehaveProject;
    private Locale lastLocale;
    
    public StoryDocument() {
        super();
    }
    
    public void setJBehaveProject(JBehaveProject project) {
        this.jbehaveProject = project;
    }
    
    public JBehaveProject getJBehaveProject() {
        return jbehaveProject;
    }

    protected void fireDocumentChanged(DocumentEvent event) {
        invalidateStoryParts();
        
        // ... continue processing
        super.fireDocumentChanged(event);
    }

    private synchronized void invalidateStoryParts() {
        parts = null;
    }
    
    private synchronized List<StoryPart> getOrGenerateStoryParts () {
        if(lastLocale==null || !lastLocale.equals(jbehaveProject.getLocale())) {
            invalidateStoryParts();
            lastLocale = jbehaveProject.getLocale();
        }
        if(parts==null) {
            parts = new StoryParser(jbehaveProject.getLocalizedStepSupport()).parse(get());
        }
        return parts;
    }
    
    public void traverseStoryParts(StoryPartVisitor visitor) {
        for(StoryPart part : getOrGenerateStoryParts ()) {
            visitor.visit(part);
            if(visitor.isDone())
                return;
        }
    }
}
