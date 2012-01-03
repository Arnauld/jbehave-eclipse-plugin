package org.technbolts.jbehave.eclipse.editors.story.completion;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.support.JBKeyword;
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
    protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region,
            int relevance) {
        final TemplateProposal p = new TemplateProposal(template,
                context, region, getImage(template), getRelevance(template, "prefix")) {
            @Override
            public String getAdditionalProposalInfo() {
                String content = super.getAdditionalProposalInfo();
                return formatTemplateToHTML(content);
            }
        };
        p.setInformationControlCreator(new IInformationControlCreator() {

            public IInformationControl createInformationControl(
                    final Shell parent) {
                return new DefaultInformationControl(parent, true) {
                    @Override
                    public void setInformation(String content) {
                        super.setInformation(content);
                    }
                };
            }
        });
        return p;
    }
    
    private static String formatTemplateToHTML(String content) {
        for(JBKeyword keyword : JBKeyword.values()) {
            String asString = keyword.asString();
            if(asString.endsWith(":"))
                asString = asString.substring(0,asString.length()-1);
            String regex = "^("+Pattern.quote(asString)+")";
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            content = pattern.matcher(content).replaceAll("<b>$1</b>");
        }
        content = content.replaceAll("[\r\n]+", "<br>");
        return content;
    }

}
