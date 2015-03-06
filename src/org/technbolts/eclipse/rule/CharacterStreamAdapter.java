package org.technbolts.eclipse.rule;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.technbolts.util.BidirectionalStream;

public class CharacterStreamAdapter implements BidirectionalStream {
    
    private ICharacterScanner scanner;
    public CharacterStreamAdapter(ICharacterScanner scanner) {
        this.scanner = scanner;
    }

 
    public int read() {
        return scanner.read();
    }
    
    public void unread() {
        scanner.unread();
    }
}
