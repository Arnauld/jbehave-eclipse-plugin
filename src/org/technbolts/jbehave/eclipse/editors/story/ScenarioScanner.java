package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.eclipse.rule.Rules.lineStartsWithRule;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.util.TextAttributeProvider;

public class ScenarioScanner extends RuleBasedScanner {
    
    public ScenarioScanner(TextAttributeProvider textAttributeProvider) {
        
        TextAttribute textAttribute = textAttributeProvider.get(StoryTextAttributes.Scenario);
        setDefaultReturnToken(new Token(textAttribute));
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.ScenarioKeyword);
        IToken keyword = new Token(textAttribute);
        
        IRule[] rules = new IRule[] {
                lineStartsWithRule("Scenario", ":", keyword)
        };
        setRules(rules);
    }

}
