package org.technbolts.jbehave.eclipse.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.ui.text.Match;
public class StepQueryParticipant implements IQueryParticipant {

    @Override
    public int estimateTicks(QuerySpecification arg0) {
        return 0;
    }

    @Override
    public IMatchPresentation getUIParticipant() {
        return null;
    }

    @Override
    public void search(ISearchRequestor requestor, QuerySpecification spec,
            IProgressMonitor monitor) throws CoreException {
        if (spec instanceof ElementQuerySpecification) {
            ElementQuerySpecification elementSpec = (ElementQuerySpecification) spec;
            int elementType = elementSpec.getElement().getElementType();
            if (elementType == IJavaElement.METHOD) {
                IAnnotation[] annotations = ((SourceMethod) elementSpec.getElement()).getAnnotations();
                if (annotations.length > 0 && annotations[0].getElementName().equals("DomainStep")) {
                    //TODO: what if no value?
                    String step = annotations[0].getMemberValuePairs()[0].getValue().toString();                    ;
                    List<Match> results = searchForStep(step, elementSpec.getElement().getJavaProject(), monitor);
                    for (Match m : results) {
                        requestor.reportMatch(m);
                    }
                }
            } else if (elementType == IJavaElement.ANNOTATION) {
                //TODO
            }
        }
    }

    private List<Match> searchForStep(String step, IJavaProject project, IProgressMonitor monitor) {
        TextSearchEngine engine = TextSearchEngine.create();
        //TODO: this can be optimized
        TextSearchScope scope = TextSearchScope.newSearchScope(
                new IResource[]{project.getResource()}, Pattern.compile("content\\.txt"), false);        
        final List<Match> matched = new LinkedList<Match>();
        TextSearchRequestor req = new TextSearchRequestor() {

            @Override
            public boolean acceptPatternMatch(TextSearchMatchAccess m)
                    throws CoreException {
                matched.add(new Match(m.getFile(), m.getMatchOffset(), m.getMatchLength()));
                return true;
            }                        
        };
        engine.search(scope, req, Pattern.compile(step), monitor);
        return matched;
    }
}
