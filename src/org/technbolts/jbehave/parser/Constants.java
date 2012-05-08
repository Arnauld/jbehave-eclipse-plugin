package org.technbolts.jbehave.parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.technbolts.util.New;

public class Constants {
    
    public static Pattern exampleTableFinder = Pattern.compile("^\\s*\\|([^-]|\\-[^-]*)", Pattern.MULTILINE);
    
    public static boolean containsExampleTable(String content) {
        return exampleTableFinder.matcher(content).find();
    }
    
    public interface TokenizerCallback {
        void token(int startOffset, int endOffset, String token, boolean isDelimiter);
    }
    
    public static Pattern lineSplitter = Pattern.compile("[\r\n]+");
    public static void splitLine(String input, TokenizerCallback callback) {
        tokenize(lineSplitter, input, callback);
    }

    public static void tokenize(Pattern pattern, String input, TokenizerCallback callback) {
        Matcher matcher = pattern.matcher(input);
        int index = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if(start>index) {
                callback.token(index, start, input.substring(index, start), false);
            }
            callback.token(start, end, input.substring(start, end), true);
            index = end;
        }
        if(index<input.length())
            callback.token(index, input.length(), input.substring(index), false);
    }
    
    public static Pattern commentLineMatcher = Pattern.compile("^\\s*!--[^\r\n]*[\r\n]{0,2}", Pattern.MULTILINE); 
    
    public static String removeComment(String input) {
        return commentLineMatcher.matcher(input).replaceAll("");
    }
    
    public static String removeTrailingComment(String input) {
        class Tok {
            String content;
            boolean delim;
            public Tok(String content, boolean delim) {
                super();
                this.content = content;
                this.delim = delim;
            }
        }
        final List<Tok> toks = New.arrayList();
        tokenize(commentLineMatcher, input, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String token, boolean isDelimiter) {
                toks.add(new Tok(token, isDelimiter));
            }
        });
        int lastIndex = toks.size()-1;
        for(;lastIndex>=0;lastIndex--) {
            if(!toks.get(lastIndex).delim)
                break;
        }
        
        if(lastIndex == toks.size()-1)
            // nothing to remove, return as is
            return input;
        
        StringBuilder builder = new StringBuilder ();
        for(int i=0;i<=lastIndex;i++)
            builder.append(toks.get(i).content);
        return builder.toString();
    }
    

}
