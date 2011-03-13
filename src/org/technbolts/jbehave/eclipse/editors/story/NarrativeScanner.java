package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.eclipse.rule.Rules.lineStartsWithRule;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.util.TextAttributeProvider;

public class NarrativeScanner extends RuleBasedScanner {

    public NarrativeScanner(TextAttributeProvider textAttributeProvider) {
        TextAttribute textAttribute = textAttributeProvider.get(StoryTextAttributes.Narrative);
        setDefaultReturnToken(new Token(textAttribute));
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.NarrativeKeyword);
        IToken keyword = new Token(textAttribute);
        
        IRule[] rules = new IRule[] {
                lineStartsWithRule("Narrative:", " ", keyword), //
                lineStartsWithRule("In order to", " ", keyword), //
                lineStartsWithRule("As a", " ", keyword), //
                lineStartsWithRule("I want to", " ", keyword) //
        };
        setRules(rules);
    }
}
