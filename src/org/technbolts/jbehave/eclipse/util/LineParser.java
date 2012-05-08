package org.technbolts.jbehave.eclipse.util;

import static org.technbolts.util.StringEnhancer.enhanceString;
import static org.technbolts.util.Strings.s;

import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.util.StringEnhancer;
import org.technbolts.util.Strings;

public class LineParser {
    
    public static boolean isTheStartIgnoringCaseOfStep(LocalizedStepSupport localizedStepSupport, String line) {
        return enhanceString(line).isTheStartIgnoringCaseOfOneOf(//
                localizedStepSupport.lGiven(true), //
                localizedStepSupport.lWhen(true), //
                localizedStepSupport.lThen(true), //
                localizedStepSupport.lAnd(true));
    }

    public static boolean isStepIgnoringCase(LocalizedStepSupport localizedStepSupport, String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf(//
                localizedStepSupport.lGiven(true), //
                localizedStepSupport.lWhen(true), //
                localizedStepSupport.lThen(true), //
                localizedStepSupport.lAnd(true));
    }
    
    public static boolean isStepType(LocalizedStepSupport localizedStepSupport, String line) {
        return enhanceString(line).equalsToOneOf(//
                localizedStepSupport.lGiven(true), //
                localizedStepSupport.lWhen(true), //
                localizedStepSupport.lThen(true), //
                localizedStepSupport.lAnd(true));
    }
    
    public static boolean isStepAndType(LocalizedStepSupport localizedStepSupport, String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf(localizedStepSupport.lAnd(true));
    }

    
    public static int stepSentenceIndex(LocalizedStepSupport localizedStepSupport, String line) {
        StringEnhancer enhanced = enhanceString(line);
        for(String prefix : s(//
                localizedStepSupport.lGiven(true), //
                localizedStepSupport.lWhen(true), //
                localizedStepSupport.lThen(true), //
                localizedStepSupport.lAnd(true))) {
            if(enhanced.startsIgnoringCaseWith(prefix))
                return prefix.length();
        }
        return 0;
    }
    
    /**
     * Remove the step keyword from the given line.
     * <strong>In case of multiline step</strong> you may prefer to use the
     * {@link #extractStepSentenceAndRemoveTrailingNewlines(String)} alternative.
     * @param line
     * @return
     */
    public static String extractStepSentence(LocalizedStepSupport localizedStepSupport, String line) {
        int indexOf = stepSentenceIndex(localizedStepSupport, line);
        return line.substring(indexOf);
    }
    
    public static String extractStepSentenceAndRemoveTrailingNewlines(LocalizedStepSupport localizedStepSupport, String text) {
        return Strings.removeTrailingNewlines(extractStepSentence(localizedStepSupport, text));
    }

    public static String stepType(LocalizedStepSupport localizedStepSupport, String stepLine) {
        StringEnhancer enhanced = enhanceString(stepLine);
        if(enhanced.startsIgnoringCaseWith(localizedStepSupport.lWhen(true)))
            return StepType.WHEN.name();
        else if(enhanced.startsIgnoringCaseWith(localizedStepSupport.lGiven(true)))
            return StepType.GIVEN.name();
        else if(enhanced.startsIgnoringCaseWith(localizedStepSupport.lThen(true)))
            return StepType.THEN.name();
        return null;
    }
}
