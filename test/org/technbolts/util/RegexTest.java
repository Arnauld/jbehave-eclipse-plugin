package org.technbolts.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.technbolts.jbehave.support.JBKeyword;

public class RegexTest {

    private static Pattern parameterPattern = Pattern.compile("\\$[a-zA-Z0-9\\-_]+");
    
    @Test
    public void usecaseEx1() {
        String text = "When a user clicks on $button button";
        
        Matcher matcher = parameterPattern.matcher(text);
        int prev = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end   = matcher.end();
            System.out.println("T>" + text.substring(prev, start) + "<");
            System.out.println("I>" + text.substring(start, end) +"<");
            prev = end;
        }
        if(prev<text.length())
            System.out.println("T>" + text.substring(prev));
    }
    
    @Test
    public void usecaseEx2 () {
        String content = "As a developer\nI want to develop efficiently";
        for(JBKeyword keyword : JBKeyword.values()) {
            String asString = keyword.asString();
            if(asString.endsWith(":"))
                asString = asString.substring(0,asString.length()-1);
            String regex = "^("+Pattern.quote(asString)+")";
            System.out.println(regex);
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            content = pattern.matcher(content).replaceAll("<b>$1</b>");
        }
        System.out.println(content);
    }
}
