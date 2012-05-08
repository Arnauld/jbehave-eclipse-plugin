package org.technbolts.jbehave.eclipse.editors.story.outline;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartVisitor;
import org.technbolts.util.New;

public class OutlineModelBuilder extends StoryPartVisitor {
    
    private final LocalizedStepSupport jbehaveProject;
    private final IDocument document;
    private List<OutlineModel> models;
    
    public OutlineModelBuilder(LocalizedStepSupport jbehaveProject, IDocument document) {
        this.jbehaveProject = jbehaveProject;
        this.document = document;
    }
    
    public List<OutlineModel> build () {
        models = New.arrayList();
        new StoryPartDocumentUtils(jbehaveProject).traverseStoryParts(document, this);
        return models;
    }
    
    @Override
    public final void done() {
        // prevent any state change, since one can reuse the visitor behavior
    }

    @Override
    public void visit(StoryPart part) {
        OutlineModel model = new OutlineModel(
                part.getPreferredKeyword(), 
                part.getContent(), 
                part.getOffset(), 
                part.getLength());
        
        if(!acceptModel(model)) {
            return;
        }
        
        if(models.isEmpty()) {
            models.add(model);
            return;
        }
        
        // pick last, merge it or add it to the list
        OutlineModel last = models.get(models.size()-1);
        if(!last.merge(model))
            models.add(model);
    }

    protected boolean acceptModel(OutlineModel model) {
        switch(model.getPartition()) {
            case Narrative:
            case Scenario:
            case ExampleTable:
            case Step:
                return true;
        }
        return false;
    }
    
}
