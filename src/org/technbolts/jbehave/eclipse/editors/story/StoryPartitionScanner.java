package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.jbehave.support.JBPartition.partitionOf;

import java.util.List;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.rule.CharacterStreamAdapter;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.JBPartition;
import org.technbolts.util.BidirectionalReader;
import org.technbolts.util.New;

public class StoryPartitionScanner extends RuleBasedPartitionScanner {

    public StoryPartitionScanner() {
        List<IPredicateRule> rules = New.arrayList();
        for(JBPartition partition : JBPartition.values())
            rules.add(predicateFor(partition));
        setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
    }
    
    private IPredicateRule predicateFor(JBPartition partition) {
        return new StoryPredicateRule(partition);
    }
    
    class StoryPredicateRule extends StoryParserRule implements IPredicateRule {
        
        private JBPartition partition;
        private Token token;
        
        private StoryPredicateRule(JBPartition partition) {
            super(true);
            this.partition = partition;
            this.token = new Token(partition.name());
        }
        @Override
        public IToken evaluate(ICharacterScanner characterScanner) {
            // predicate contract... one type of token...
            // if not the good one, simply reset
            CharacterStreamAdapter stream = new CharacterStreamAdapter(characterScanner);
            BidirectionalReader scanner = new BidirectionalReader(stream);
            int position = scanner.getPosition();
            IToken token = super.nextToken(scanner);
            if(token.isUndefined())
                scanner.backToPosition(position);
            return token;
        }
        
        @Override
        public IToken evaluate(ICharacterScanner characterScanner, boolean resume) {
            return evaluate(characterScanner);
        }

        @Override
        public IToken getSuccessToken() {
            return token;
        }
        
        @Override
        protected boolean isKeywordStop(JBKeyword keywordRead, JBKeyword keyword) {
            // make sure each step is in its own partition
            if(JBKeyword.isStep(keyword))
                return true;
            return super.isKeywordStop(keywordRead, keyword);
        }
        
        @Override
        protected IToken tokenOf(JBKeyword keyword) {
            if(keyword!=null && partitionOf(keyword)==partition)
                return token;
            return Token.UNDEFINED;
        }
    }
}
