package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.util.ArrayUtils;

public class StoryPartitionScannerBak extends RuleBasedPartitionScanner {

    public final static String COMMENT = "__jbehave_comment";
    public static final String SCENARIO = "__jbehave_scenario";
    public static final String[] PARTITION_TYPES = new String[] { SCENARIO, COMMENT };

    public StoryPartitionScannerBak() {

        IToken scenario = new Token(SCENARIO);
        IToken comment = new Token(COMMENT);

        IPredicateRule[] rules = ArrayUtils.<IPredicateRule> toArray(//
                lineStartsWithRule("Narrative:", "", comment), //
                lineStartsWithRule("In order to ", "", comment), //
                lineStartsWithRule("As a ", "", comment), //
                lineStartsWithRule("I want to ", "", comment),//
                lineStartsWithRule("Scenario:", "", comment),//
                lineStartsWithRule("!--", "", comment),//
                //
                lineStartsWithRule("Given", "", scenario), //
                lineStartsWithRule("When", "", scenario), //
                lineStartsWithRule("Then", "", scenario), //
                lineStartsWithRule("And", "", scenario) //
                );
        setPredicateRules(rules);
    }

    private IPredicateRule lineStartsWithRule(String startSequense, String endSequence, IToken token) {
        SingleLineRule startsWithRule = new SingleLineRule(startSequense, endSequence, token);
        startsWithRule.setColumnConstraint(0);
        return startsWithRule;
    }

}
