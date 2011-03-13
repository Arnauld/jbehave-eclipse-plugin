package org.technbolts.jbehave.eclipse.util;

import static org.technbolts.util.StringEnhancer.enhanceString;

public class LineParser {
    public static boolean isStep(String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf("given ", "when ", "then ", "and ");
    }
    
    public static String extractStepSentence(String line) {
        if(!isStep(line))
            return null;
        int indexOf = line.indexOf(' ');
        return line.substring(indexOf+1);
    }

    public static String stepType(String stepLine) {
        if(isStep(stepLine)) {
            int indexOf = stepLine.indexOf(' ');
            return stepLine.substring(0, indexOf);
        }
        return null;
    }
}
