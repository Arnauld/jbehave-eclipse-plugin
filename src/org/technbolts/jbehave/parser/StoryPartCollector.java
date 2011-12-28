package org.technbolts.jbehave.parser;

import java.util.List;

import org.technbolts.util.New;

public class StoryPartCollector implements StoryPartVisitor {
    
    private final List<StoryPart> parts = New.arrayList();

    @Override
    public void visit(StoryPart part) {
        parts.add(part);
    }
    
    public List<StoryPart> getParts() {
        return parts;
    }
}
