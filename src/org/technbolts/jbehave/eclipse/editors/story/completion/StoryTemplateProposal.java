package org.technbolts.jbehave.eclipse.editors.story.completion;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

public class StoryTemplateProposal extends TemplateProposal {

    public StoryTemplateProposal(final Template template,
            final TemplateContext context, final IRegion region,
            final Image image, final int relevance) {
        super(template, context, region, image, relevance);
    }
}
