package org.technbolts.jbehave.parser;

public abstract class StoryPartVisitor {
    
    private boolean isDone = false;

    public abstract void visit(StoryPart part);
    
    public boolean isDone() {
        return isDone;
    }

    public void done() {
        this.isDone = true;
    }
}
