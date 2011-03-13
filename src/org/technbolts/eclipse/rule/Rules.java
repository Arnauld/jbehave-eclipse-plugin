package org.technbolts.eclipse.rule;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

public class Rules {
    
    public static IPredicateRule lineStartsWithRule(String startSequense, String endSequence, IToken token) {
        SingleLineRule startsWithRule = new SingleLineRule(startSequense, endSequence, token);
        startsWithRule.setColumnConstraint(0);
        return startsWithRule;
    }
}
