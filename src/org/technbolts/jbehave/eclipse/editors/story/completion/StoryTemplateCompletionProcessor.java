package org.technbolts.jbehave.eclipse.editors.story.completion;

import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.util.New;

public class StoryTemplateCompletionProcessor extends TemplateCompletionProcessor {
    
    private List<Template> additionals = New.arrayList();

    public StoryTemplateCompletionProcessor() {
        super();
    }
    
    public void additional(Template template) {
        additionals.add(template);
    }

    @Override
    protected TemplateContextType getContextType(final ITextViewer viewer,
            final IRegion region) {
        return Activator.getDefault().getContextTypeRegistry()
                .getContextType(StoryContextType.STORY_CONTEXT_TYPE_ID);
    }

    @Override
    protected Image getImage(final Template template) {
        return null;
    }

    @Override
    protected Template[] getTemplates(final String contextTypeId) {
        List<Template> list = New.arrayList();
        list.addAll(additionals);
        Activator activator = Activator.getDefault();
        for(Template template : activator.getTemplateStore().getTemplates()) {
            list.add(template);
        }
        return list.toArray(new Template[list.size()]);
    }

    @Override
    protected ICompletionProposal createProposal(final Template template,
            final TemplateContext context, final IRegion region,
            final int relevance) {
        final StoryTemplateProposal p = new StoryTemplateProposal(template,
                context, region, getImage(template), relevance);
        /*
        p.setInformationControlCreator(new IInformationControlCreator() {

            public IInformationControl createInformationControl(
                    final Shell parent) {
                return new SourceViewerInformationControl(parent,
                        PreferenceConstants.EDITOR_TEXT_FONT);
            }
        });
        */
        return p;
    }

}
