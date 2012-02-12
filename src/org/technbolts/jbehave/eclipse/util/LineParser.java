package org.technbolts.jbehave.eclipse.util;

import static org.technbolts.util.StringEnhancer.enhanceString;

import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.JBehaveProject;
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
        if(!isStepIgnoringCase(project, line))
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
    public static String extractStepSentence(JBehaveProject project, String line) {
        int indexOf = stepSentenceIndex(project, line);
        return line.substring(indexOf);
    }
    
    public static String extractStepSentenceAndRemoveTrailingNewlines(JBehaveProject project, String text) {
        return Strings.removeTrailingNewlines(extractStepSentence(project, text));
    }

    public static String stepType(JBehaveProject project, String stepLine) {
        if(isStepIgnoringCase(project, stepLine)) {
            int indexOf = stepLine.indexOf(' ');
            String localizedKeyword = stepLine.substring(0, indexOf);
            if (localizedKeyword.equalsIgnoreCase(project.lWhen(false))) {
            	return StepType.WHEN.name();
            } else if (localizedKeyword.equalsIgnoreCase(project.lGiven(false))) {
            	return StepType.GIVEN.name();
            } else if (localizedKeyword.equalsIgnoreCase(project.lThen(false))) {
            	return StepType.THEN.name();
            }
        }
        return null;
    }

}
