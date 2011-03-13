package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.eclipse.rule.Rules.lineStartsWithRule;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.technbolts.eclipse.util.TextAttributeProvider;

public class StepScanner extends RuleBasedScanner {

    public StepScanner(TextAttributeProvider textAttributeProvider) {
        
        TextAttribute textAttribute = textAttributeProvider.get(StoryTextAttributes.Step);
        setDefaultReturnToken(new Token(textAttribute));
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepKeyword);
        IToken keyword = new Token(textAttribute);
        
        textAttribute = textAttributeProvider.get(StoryTextAttributes.StepParameter);
        IToken parameter = new Token(textAttribute);
        
                
        IRule[] rules = new IRule[] {
                lineStartsWithRule("Given", " ", keyword), //
                lineStartsWithRule("When", " ", keyword), //
                lineStartsWithRule("Then", " ", keyword), //
                lineStartsWithRule("And", " ", keyword), //
                new WordRule(new ParameterWordDetector(), parameter)
        };
        setRules(rules);
    }
    
}
