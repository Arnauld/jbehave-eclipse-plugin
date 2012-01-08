package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.jbehave.eclipse.util.StoryPartDocumentUtils.findStoryPartAtRegion;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.technbolts.jbehave.eclipse.util.StepUtils;
import org.technbolts.jbehave.parser.StoryPart;
import org.technbolts.util.Ref;

public class StepHyperLinkDetector implements IHyperlinkDetector {

    private IHyperlink[] NONE = null;// new IHyperlink[0];

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer viewer, final IRegion region,
            boolean canShowMultipleHyperlinks) {

        final Ref<StoryPart> found = findStoryPartAtRegion(viewer.getDocument(), region);
        if (found.isNull()) {
            return NONE;
        }

        final StoryPart part = found.get();
        if (!part.isStepPart())
            return NONE;
        final String step = part.extractStepSentenceAndRemoveTrailingNewlines();

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
