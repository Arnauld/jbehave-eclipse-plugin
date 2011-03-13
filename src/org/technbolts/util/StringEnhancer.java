package org.technbolts.util;

public class StringEnhancer {

    public static StringEnhancer enhanceString(String value) {
        return new StringEnhancer(value);
    }
    
    private final String underlying;

    public StringEnhancer(String underlying) {
        super();
        this.underlying = underlying;
    }
    
    public boolean isNull () {
        return underlying==null;
    }
    
    public boolean startsIgnoringCaseWithOneOf(String...prefixes) {
        return startsWithOneOf(true, prefixes);
    }
    
    public boolean startsWithOneOf(String...prefixes) {
        return startsWithOneOf(false, prefixes);
    }
    
    public boolean startsWithOneOf(boolean ignoreCase, String...prefixes) {
        if(isNull())
            return false;
        for(String prefix:prefixes) {
            if(underlying.regionMatches(ignoreCase, 0, prefix, 0, prefix.length()))
                return true;
        }
        return false;
    }
    
    public boolean endsWithOneOf(String...suffixes) {
        if(isNull())
            return false;
        for(String suffix:suffixes) {
            if(underlying.endsWith(suffix))
                return true;
        }
        return false;
    }

}
