package org.technbolts.jbehave.eclipse.editors.story;

import static org.technbolts.jbehave.support.JBPartition.partitionOf;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.technbolts.eclipse.rule.CharacterStreamAdapter;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.jbehave.support.StoryParser;
import org.technbolts.util.BidirectionalReader;
import org.technbolts.util.BidirectionalStream;

public class StoryParserRule extends StoryParser implements IRule {

    public StoryParserRule(boolean partition) {
        super(partition);
    }
    
    @Override
    public IToken evaluate(ICharacterScanner characterScanner) {
        BidirectionalReader scanner = new BidirectionalReader(adapt(characterScanner));
        return nextToken(scanner);
    }
    
    private BidirectionalStream adapt(ICharacterScanner characterScanner) {
        return new CharacterStreamAdapter(characterScanner);
    }
    
    public IToken nextToken(BidirectionalReader scanner) {
        JBKeyword keyword = nextKeyword(scanner);
        if(scanner.eof())
            return Token.EOF;
        return tokenOf(keyword);
    }

    protected IToken tokenOf(JBKeyword keyword) {
        if(keyword!=null) {
            if(partition)
                return new Token(partitionOf(keyword));
            else
                return new Token (keyword);
        }
        return Token.UNDEFINED;
    }

}
