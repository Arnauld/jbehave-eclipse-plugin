package org.technbolts.jbehave.eclipse.editors.story.quicksearch;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.technbolts.jbehave.eclipse.PotentialStep;

public class QuickSearchTreeContentProvider implements ITreeContentProvider {

    private final Object[] NO_CHILDREN = new Object[] {};

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public Object[] getChildren(Object parent) {
        if(parent instanceof List) {
            @SuppressWarnings("unchecked")
            List<PotentialStep> candidates = (List<PotentialStep>)parent;
            return candidates.toArray();
        }
        return NO_CHILDREN;
    }

    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    public Object getParent(Object child) {
        return null;
    }

    public boolean hasChildren(Object parent) {
        if(parent instanceof List) {
            @SuppressWarnings("unchecked")
            List<PotentialStep> candidates = (List<PotentialStep>)parent;
            return !candidates.isEmpty();
        }
        return false;
    }

    
}
