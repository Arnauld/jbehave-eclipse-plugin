package org.technbolts.jbehave.eclipse.util;

import static org.technbolts.util.StringEnhancer.enhanceString;
import static org.technbolts.jbehave.eclipse.JBehaveProject.*;

import org.jbehave.core.steps.StepType;
import org.technbolts.util.Strings;

public class LineParser {
    
    public static boolean isTheStartIgnoringCaseOfStep(String line) {
        return enhanceString(line).isTheStartIgnoringCaseOfOneOf(lGiven(true), lWhen(true), lThen(true), lAnd(true));
    }

    public static boolean isStepIgnoringCase(String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf(lGiven(true), lWhen(true), lThen(true),lAnd(true));
    }
    
    public static boolean isStepType(String line) {
        return enhanceString(line).equalsToOneOf(lGiven(true), lWhen(true), lThen(true),lAnd(true));
    }
    
    public static boolean isStepAndType(String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf(lAnd(true));
    }

    
    public static int stepSentenceIndex(String line) {
        if(!isStepIgnoringCase(line))
            return 0;
        int indexOf = line.indexOf(' '); // TODO: Handle when keyword contains more than one word
        return indexOf+1;
    }
    
    /**
     * Remove the step keyword from the given line.
     * <strong>In case of multiline step</strong> you may prefer to use the
     * {@link #extractStepSentenceAndRemoveTrailingNewlines(String)} alternative.
     * @param line
     * @return
     */
    public static String extractStepSentence(String line) {
        int indexOf = stepSentenceIndex(line);
        return line.substring(indexOf);
    }
    
    public static String extractStepSentenceAndRemoveTrailingNewlines(String text) {
        return Strings.removeTrailingNewlines(extractStepSentence(text));
    }

    public static String stepType(String stepLine) {
        if(isStepIgnoringCase(stepLine)) {
            int indexOf = stepLine.indexOf(' ');
            String localizedKeyword = stepLine.substring(0, indexOf);
            if (localizedKeyword.equalsIgnoreCase(lWhen(false))) {
            	return StepType.WHEN.name();
            } else if (localizedKeyword.equalsIgnoreCase(lGiven(false))) {
            	return StepType.GIVEN.name();
            } else if (localizedKeyword.equalsIgnoreCase(lThen(false))) {
            	return StepType.THEN.name();
            }
        }
        return null;
    }

}
