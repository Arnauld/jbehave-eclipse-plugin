package org.technbolts.jbehave.eclipse.editors.story.completion;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.support.JBKeyword;

public class StoryContextType extends TemplateContextType {

    /** This context's id */
    public static final String STORY_CONTEXT_TYPE_ID = "org.technbolts.jbehave.story"; //$NON-NLS-1$

    /**
     * Creates a new XML context type.
     */
    public StoryContextType() {
        addGlobalResolvers();
    }

    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
        for(JBKeyword keyword : JBKeyword.values()) {
            addResolver(new LocalizedKeywordResolver(keyword));
        }
    }

    public static TemplateContextType getTemplateContextType() {
        return Activator.getDefault().getContextTypeRegistry()
                .getContextType(StoryContextType.STORY_CONTEXT_TYPE_ID);
    }
    
}
