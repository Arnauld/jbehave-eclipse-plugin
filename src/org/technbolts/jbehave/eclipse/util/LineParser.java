package org.technbolts.jbehave.eclipse.util;

import static org.technbolts.util.StringEnhancer.enhanceString;
import static org.technbolts.util.Strings.s;

import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.JBehaveProject;
import org.technbolts.util.StringEnhancer;
import org.technbolts.util.Strings;

public class LineParser {
    
    public static boolean isTheStartIgnoringCaseOfStep(JBehaveProject project, String line) {
        return enhanceString(line).isTheStartIgnoringCaseOfOneOf(//
                project.lGiven(true), //
                project.lWhen(true), //
                project.lThen(true), //
                project.lAnd(true));
    }

    public static boolean isStepIgnoringCase(JBehaveProject project, String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf(//
                project.lGiven(true), //
                project.lWhen(true), //
                project.lThen(true), //
                project.lAnd(true));
    }
    
    public static boolean isStepType(JBehaveProject project, String line) {
        return enhanceString(line).equalsToOneOf(//
                project.lGiven(true), //
                project.lWhen(true), //
                project.lThen(true), //
                project.lAnd(true));
    }
    
    public static boolean isStepAndType(JBehaveProject project, String line) {
        return enhanceString(line).startsIgnoringCaseWithOneOf(project.lAnd(true));
    }

    
    public static int stepSentenceIndex(JBehaveProject project, String line) {
        StringEnhancer enhanced = enhanceString(line);
        for(String prefix : s(//
                project.lGiven(true), //
                project.lWhen(true), //
                project.lThen(true), //
                project.lAnd(true))) {
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
    public static String extractStepSentence(JBehaveProject project, String line) {
        int indexOf = stepSentenceIndex(project, line);
        return line.substring(indexOf);
    }
    
    public static String extractStepSentenceAndRemoveTrailingNewlines(JBehaveProject project, String text) {
        return Strings.removeTrailingNewlines(extractStepSentence(project, text));
    }

    public static String stepType(JBehaveProject project, String stepLine) {
        StringEnhancer enhanced = enhanceString(stepLine);
        if(enhanced.startsIgnoringCaseWith(project.lWhen(true)))
            return StepType.WHEN.name();
        else if(enhanced.startsIgnoringCaseWith(project.lGiven(true)))
            return StepType.GIVEN.name();
        else if(enhanced.startsIgnoringCaseWith(project.lThen(true)))
            return StepType.THEN.name();
        return null;
    }
}
