package org.technbolts.jbehave.eclipse.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

public class StepSearchRequestor extends SearchRequestor {
    
    String step;
    public ResolvedSourceMethod methodToJump;

    public StepSearchRequestor(String step) {
        this.step = step;
    }

    @SuppressWarnings("restriction")
    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
        if (match.getElement() instanceof ResolvedSourceMethod) {
            ResolvedSourceMethod methodToJump = (ResolvedSourceMethod) match.getElement(); 
            System.out.println(methodToJump);
            //TODO: what if there is no annotation :)
            String stepRegEx = methodToJump.getAnnotation("DomainStep").getMemberValuePairs()[0].getValue().toString();
            if (step.matches(stepRegEx)) {
                this.methodToJump = methodToJump;
            }
        }
    }

}
