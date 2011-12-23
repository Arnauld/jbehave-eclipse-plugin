package org.technbolts.jbehave.eclipse.util;

import static org.technbolts.util.StringEnhancer.enhanceString;

public class LineParser {
    
    public static boolean isTheStartIgnoringCaseOfOneOf(String line) {
        return enhanceString(line).isTheStartIgnoringCaseOfOneOf("given ", "when ", "then ", "and ");
    }

    public static boolean isStepIgnoringCase(String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf("given ", "when ", "then ", "and ");
    }
    
    public static boolean isStepType(String line) {
        return enhanceString(line).equalsToOneOf("Given ", "When ", "Then ", "And ");
    }
    
    public static String extractStepSentence(String line) {
        if(!isStepIgnoringCase(line))
            return null;
        int indexOf = line.indexOf(' ');
        return line.substring(indexOf+1);
    }

    public static String stepType(String stepLine) {
        if(isStepIgnoringCase(stepLine)) {
            int indexOf = stepLine.indexOf(' ');
            return stepLine.substring(0, indexOf);
        }
        return null;
    }

}
