package org.technbolts.util;

import java.util.ArrayList;
import java.util.List;

import fj.F;

public class ParametrizedString {
    
    private String content;
    private List<Token> tokens = new ArrayList<Token>();
    
    public ParametrizedString(String content) {
        if(content==null)
            throw new IllegalArgumentException("Content cannot be null");
        this.content = content;
        parse();
    }
    
    @Override
    public int hashCode() {
        return content.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ParametrizedString))
            return false;
        return isSameAs((ParametrizedString)obj);
    }
    
    public boolean isSameAs(ParametrizedString other) {
        return other.content.equals(content);
    }
    
    public String getContent() {
        return content;
    }

    private void parse() {
        boolean inIdentifier = false;
        StringBuilder buffer = new StringBuilder ();
        
        char previous = ' ';
        int i,n=content.length();
        for(i=0; i<n; i++) {
            char c = content.charAt(i);
            try {
                if(inIdentifier) {
                    if(isIdentifierChar(c)) {
                        buffer.append(c);
                        continue;
                    }
                    else {
                        add(new Token(i-buffer.length(), buffer.length(), true));
                        buffer.setLength(0);
                        inIdentifier = false;
                        // continue parsing, incase of the character is an identifier delimiter
                    }
                }
                
                // make sure it is not escaped
                if(c=='$' && previous!='\\') {
                    add(new Token(i-buffer.length(), buffer.length(), false));
                    buffer.setLength(0);
                    inIdentifier = true;
                }
                else {
                    buffer.append(c);
                }
            }
            finally {
                previous = c;
            }
        }
        
        //remaining
        add(new Token(i-buffer.length(), buffer.length(), inIdentifier));
    }
    
    private void add(Token token) {
        tokens.add(token);
    }

    public class Token {
        public final int offset;
        public final int length;
        public final boolean isIdentifier;
        public Token(int offset, int length, boolean isIdentifier) {
            this.offset = offset;
            this.length = length;
            this.isIdentifier = isIdentifier;
        }
        public String value () {
            return content.substring(offset, offset + length);
        }
        @Override
        public String toString() {
            return "<<"+(isIdentifier?"$":"")+value()+">>";
        }
        public boolean regionMatches(int toffset, String other, int ooffset, int len) {
            return content.regionMatches(offset + toffset, other, ooffset, len);
        }
    }
    
    public static boolean isIdentifierChar(char c) {
        return   ('a' <= c && c <= 'z')
               ||('A' <= c && c <= 'Z')
               ||('0' <= c && c <= '9')
               ||(c=='-' || c=='_');
    }
    
    public Token getToken(int index) {
        return tokens.get(index);
    }
    
    public List<Token> getTokens() {
        return new ArrayList<Token>(tokens);
    }

    public List<String> getParameters() {
        List<String> parameters = new ArrayList<String>();
        for(Token token : tokens) {
            if(token.isIdentifier)
                parameters.add(token.value());
        }
        return parameters;
    }

    public int getTokenCount() {
        return tokens.size();
    }

    private int parameterCount = -1;
    public int getParameterCount() {
        if(parameterCount == -1) {
            parameterCount = parameterCount();
        }
        return parameterCount;
    }
    
    private int parameterCount() {
        return FJ.count(tokens, isIdentifier());
    }
    
    public static fj.F<Token,Boolean> isIdentifier() {
        return new F<ParametrizedString.Token, Boolean>() {
            @Override
            public Boolean f(Token token) {
                return token.isIdentifier;
            }
        };
    }
    
    public float weightOf(String input) {
        return ((float)acceptsBeginning(input))/((float)getTokenCount());
    }
    
    public int acceptsBeginning(String input) {
        WeightChain chain = calculateWeightChain(input);
        return chain.getWeight();
    }

    public WeightChain calculateWeightChain(String input) {
        WeightChain chain = acceptsBeginning(0, input, 0);
        chain.input = input;
        chain.collectWeights();
        return chain;
    }

    private WeightChain acceptsBeginning(int inputIndex, String input, int tokenIndexStart) {
        WeightChain pair = new WeightChain();
        pair.inputIndex = inputIndex;
        
        WeightChain current = pair;
        
        List<Token> tokens = this.tokens;
        for(int tokenIndex=tokenIndexStart,n=tokens.size(); tokenIndex<n; tokenIndex++) {
            boolean isLastToken = (tokenIndex==n-1);
            Token token = tokens.get(tokenIndex);
            if(!token.isIdentifier) {
                int remaining = input.length()-inputIndex;
                if(remaining>token.length && isLastToken) {
                    // more data than the token itself
                    return WeightChain.zero();
                }
                
                int overlaping = Math.min(token.length, remaining);
                if(overlaping>0) {
                    if(token.regionMatches(0, input, inputIndex, overlaping)) {
                        current.tokenIndex = tokenIndex;
                        current.weight++;
                        if(overlaping == token.length) // full token match 
                        {
                            current.weight++;
                            if((inputIndex + overlaping)==input.length())
                                // no more data, break the loop now
                                return pair;
                        }
                        else // break looop
                            return pair;
                        
                        inputIndex += overlaping;
                        WeightChain next = new WeightChain();
                        next.inputIndex = inputIndex;
                        current.next = next;
                        current = next;
                    }
                    else {
                        // no match
                        return WeightChain.zero();
                    }
                }
                else {
                    // not enough data, returns what has been collected
                    return pair;
                }
            }
            else {
                current.tokenIndex = tokenIndex;
                current.weight++;
                
                // not the most efficient part, but no other solution right now
                WeightChain next = WeightChain.zero();
                for(int j=inputIndex+1; j<input.length(); j++) {
                    WeightChain sub = acceptsBeginning(j, input, tokenIndex+1);
                    if(sub.isWeighterThan(next)) {
                        next = sub;
                    }
                }
                current.next = next;
                return pair;
            }
        }
        return pair;
    }
    
    public static class WeightChain {
        public static final WeightChain zero() {
            return new WeightChain();
        }
        private String input;
        private int inputIndex;
        private int weight;
        private int tokenIndex = -1;
        private WeightChain next;
        
        public WeightChain last() {
            WeightChain last = this;
            WeightChain iter = this;
            while(iter!=null) {
                if(!iter.isZero())
                    last = iter;
                iter = iter.next;
            }
            return last;
        }
        public boolean isZero() {
            return weight==0 && tokenIndex==-1;
        }
        public int getInputIndex() {
            return inputIndex;
        }
        public WeightChain getNext() {
            return next;
        }
        public int getWeight() {
            return weight;
        }
        public int getTokenIndex() {
            return tokenIndex;
        }
        public boolean isWeighterThan(WeightChain pair) {
            if(weight>pair.weight)
                return true;
            return false;
        }
        
        @Override
        public String toString() {
            return "WeightChain [inputIndex=" + inputIndex + ", weight=" + weight + ", tokenIndex=" + tokenIndex + "]";
        }
        public void collectWeights() {
            int w = weight;
            WeightChain n = next;
            while(n!=null) {
                if(!n.isZero()) {
                    w += n.weight;
                }
                n = n.next;
            }
            
            this.weight = w;
        }

        public List<String> tokenize() {
            List<String> parts = new ArrayList<String>();
            if(isZero())
                return parts;

            int indexBeg = inputIndex;
            WeightChain n = next;
            while(n!=null) {
                if(!n.isZero()) {
                    parts.add(input.substring(indexBeg, n.inputIndex));
                    indexBeg = n.inputIndex;
                }
                n = n.next;
            }
            parts.add(input.substring(indexBeg));
            
            return parts;
        }
    }
    
    public boolean matches(String input) {
        return acceptsBeginning(input) == (2*getTokenCount()-getParameterCount());
    }

    public String complete(String input) {
        WeightChain chain = calculateWeightChain(input);
        WeightChain last  = chain.last();
        if(last.isZero())
            return "";
        int inputIndex = last.inputIndex;
        int tokenIndex = last.tokenIndex;
        
        StringBuilder builder = new StringBuilder();
        
        Token token = getToken(tokenIndex);
        if(!token.isIdentifier) {
            int consumed = input.length() - inputIndex;
            builder.append(getToken(tokenIndex).value().substring(consumed));
        }
        tokenIndex++;
        for(int i = tokenIndex; i< getTokenCount(); i++) {
            token = getToken(i);
            if(token.isIdentifier)
                builder.append("$");
            builder.append(token.value());
        }
        return builder.toString();
    }

}
