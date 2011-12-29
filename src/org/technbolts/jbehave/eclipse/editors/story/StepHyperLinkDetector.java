package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepUtils;
import org.technbolts.jbehave.parser.StoryParser;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.jbehave.parser.StoryPartVisitor;

public class StepHyperLinkDetector implements IHyperlinkDetector {
    
    private IHyperlink[] NONE = null;//new IHyperlink[0];
    
    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer viewer, final IRegion region,
            boolean canShowMultipleHyperlinks) {
        
        final StoryPart[] found = new StoryPart[1];
        new StoryParser().parse(viewer.getDocument().get(), new StoryPartVisitor() {
            @Override
            public void visit(StoryPart part) {
                if(part.intersects(region.getOffset(), region.getLength())) {
                    found[0] = part;
                }
            }
        });
        if(found[0]==null) {
            return NONE;
        }
        
        final StoryPart part = found[0];
        final String step = LineParser.extractStepSentence(part.getContent());
        if(step==null)
            return NONE;

        IHyperlink link = new IHyperlink() {

            @Override
            public IRegion getHyperlinkRegion() {
                return new Region(part.getOffset(), part.getLength());
            }

            @Override
            public String getHyperlinkText() {
                return step;
            }

            @Override
            public String getTypeLabel() {
                return "Go to step";
            }

            @Override
            public void open() {
                try {
                    StepUtils.jumpToDeclaration(viewer, step);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return new IHyperlink[] { link };
    }

}
