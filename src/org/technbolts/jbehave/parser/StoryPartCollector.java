package org.technbolts.jbehave.parser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.util.New;

public class StoryPartCollector extends StoryPartVisitor {
    
    private Logger logger = LoggerFactory.getLogger(StoryPartCollector.class);
    
    private final List<StoryPart> parts = New.arrayList();

    @Override
    public void visit(StoryPart part) {
        logger.debug("Collecting part {}", part);
        parts.add(part);
    }
    
    public List<StoryPart> getParts() {
        return parts;
    }
    
}
