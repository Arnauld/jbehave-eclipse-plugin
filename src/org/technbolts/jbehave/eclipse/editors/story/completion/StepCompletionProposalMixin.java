package org.technbolts.jbehave.eclipse.editors.story.completion;

import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.Activator;
import org.technbolts.jbehave.eclipse.ImageIds;
import org.technbolts.jbehave.eclipse.LocalizedStepSupport;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.util.LineParser;
import org.technbolts.jbehave.eclipse.util.StepLocator.WeightedCandidateStep;

public class StepCompletionProposalMixin {

    public interface Trait {
        LocalizedStepSupport getJBehaveProject();
        WeightedCandidateStep getWeightedCandidateStep();
        String getComplete();
        String getLabel();
    }
    
    public static StepType getStepType (Trait trait) {
        return trait.getWeightedCandidateStep().potentialStep.stepType;
    }
    
    private static Pattern parameterPattern = Pattern.compile("\\$[a-zA-Z0-9\\-_]+");

    public static StyledString createStyledString(Trait trait) {
        final StyledString styledString = new StyledString();
        
        String label = trait.getLabel();
        // remove step keyword if any, information is provided through the icon
        label = LineParser.extractStepSentence(trait.getJBehaveProject(), label);
        
        Matcher matcher = parameterPattern.matcher(label);
        int prev = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end   = matcher.end();
            if(start>prev)
                styledString.append(label.substring(prev, start));
            styledString.append(label.substring(start, end), StyledString.COUNTER_STYLER);
            prev = end;
        }
        if(prev<label.length())
            styledString.append(label.substring(prev));
        
        //styledString.append(" (" + trait.getWeightedCandidateStep().potentialStep.fullStep() + ")", StyledString.QUALIFIER_STYLER);
        return styledString;
    }
    
    public static String getAdditionalHTML(Trait trait) {
        PotentialStep pStep = trait.getWeightedCandidateStep().potentialStep;
        
        String htmlString = "<b>" + pStep.fullStep() + "</b>";
        htmlString += "<br><br>";
        try {
            Reader reader = JavadocContentAccess.getContentReader(pStep.method, true);
            String javadoc = IOUtils.toString(reader);//JDTUtils.getJavadocOf();
            if(StringUtils.isBlank(javadoc)) {
                javadoc = "<small>No Javadoc</small>";
            }
            htmlString += javadoc;
        } catch (Exception e) {
            htmlString += "Failed to retrieve documentation";
        }
        return htmlString;
    }
    
    public static Image getImage(Trait trait) {
        String key = null;
        switch (getStepType (trait)) {
            case GIVEN:
                key = ImageIds.STEP_GIVEN;
                break;
            case WHEN:
                key = ImageIds.STEP_WHEN;
                break;
            case THEN:
                key = ImageIds.STEP_THEN;
                break;
        }
        if (key != null)
            return Activator.getDefault().getImageRegistry().get(key);
        return null;
    }
}
