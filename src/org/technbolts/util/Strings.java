package org.technbolts.util;

import static org.technbolts.util.IO.CR;
import static org.technbolts.util.IO.LF;

import java.util.Collection;

public class Strings {
    
    public static String[] toArray(Collection<String> values) {
        if(values==null)
            return new String[0];
        return values.toArray(new String[values.size()]);
    }
    
    public static boolean sameStarts(CharSequence sequence, CharSequence prefix) {
        int len = Math.min(sequence.length(), prefix.length());
        for(int i=0;i<len;i++) {
            if(sequence.charAt(i)!=prefix.charAt(i))
                return false;
        }
        
        // still there
        return true;
    }
    
    public static boolean startsWith(CharSequence sequence, CharSequence prefix) {
        int slen = sequence.length();
        int plen = prefix.length();
        if(slen<plen)
            return false;
        return sameStarts(sequence, prefix);
    }
    
    public static String removeTrailingNewlines(String string) {
        int index = string.length();
        while(index>0) {
            index--;
            char c = string.charAt(index);
            if(c==CR || c==LF)
                continue;
            else {
                index++;
                break;
            }
        }
        if(index<string.length())
            return string.substring(0, index);
        else
            return string;
    }
}
