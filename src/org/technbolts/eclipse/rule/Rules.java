package org.technbolts.eclipse.rule;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.technbolts.util.BidirectionalReader;

public class Rules {
    
    public static IPredicateRule lineStartsWithRule(String startSequense, String endSequence, IToken token) {
        SingleLineRule startsWithRule = new SingleLineRule(startSequense, endSequence, token);
        startsWithRule.setColumnConstraint(0);
        return startsWithRule;
    }
    
    public static BidirectionalReader createReader(ICharacterScanner characterScanner) {
        BidirectionalReader reader = new BidirectionalReader(new CharacterStreamAdapter(characterScanner));
        return reader;
    }
    
    public static ICharacterScanner createScanner(IDocument document, int offset, int length) {
        return new DocumentCharacterScanner(document, offset, length);
    }
}
