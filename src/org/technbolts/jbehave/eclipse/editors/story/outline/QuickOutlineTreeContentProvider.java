package org.technbolts.jbehave.eclipse.editors.story.outline;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.technbolts.jbehave.eclipse.PotentialStep;

public class QuickOutlineTreeContentProvider implements ITreeContentProvider {

    private final Object[] NO_CHILDREN = new Object[] {};

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getChildren(Object parent) {
        if(parent instanceof List) {
            @SuppressWarnings("unchecked")
            List<PotentialStep> candidates = (List<PotentialStep>)parent;
            return candidates.toArray();
        }
        return NO_CHILDREN;
    }

    @Override
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    @Override
    public Object getParent(Object child) {
        return null;
    }

    @Override
    public boolean hasChildren(Object parent) {
        if(parent instanceof List) {
            @SuppressWarnings("unchecked")
            List<PotentialStep> candidates = (List<PotentialStep>)parent;
            return !candidates.isEmpty();
        }
        return false;
    }

    
}
