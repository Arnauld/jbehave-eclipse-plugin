package org.technbolts.jbehave.eclipse.util;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.technbolts.jbehave.eclipse.editors.story.StoryDocument;
import org.technbolts.jbehave.parser.StoryParser;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartCollector;
import org.technbolts.jbehave.parser.StoryPartVisitor;
import org.technbolts.util.Ref;

public class StoryPartDocumentUtils {

    public static List<StoryPart> getStoryParts(IDocument document) {
        StoryPartCollector collector = new StoryPartCollector();
        traverseStoryParts(document, collector);
        return collector.getParts();
    }

    public static void traverseStoryParts(IDocument document, StoryPartVisitor visitor) {
        if(document instanceof StoryDocument) {
            ((StoryDocument)document).traverseStoryParts(visitor);
        }
        else {
            new StoryParser().parse(document.get(), visitor);
        }
    }

    public static StoryPart findStoryPartAtOffset(IDocument document, final int offset) {
        final Ref<StoryPart> ref = Ref.create();
        StoryPartVisitor visitor = new StoryPartVisitor() {
            @Override
            public void visit(StoryPart part) {
                if(part.intersects(offset, 1)) {
                    ref.set(part);
                    done();
                }
            }
        };
        traverseStoryParts(document, visitor);
        return ref.get();
    }

}
