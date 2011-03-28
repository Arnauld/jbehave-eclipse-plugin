package org.technbolts.jbehave.eclipse;

import java.util.List;

import org.technbolts.util.New;

public class Visitor {
    private boolean isDone;
    private List<PotentialStep> founds = New.arrayList();
    public void visit(PotentialStep step) {
    }
    boolean isDone () {
        return isDone;
    }
    public void done () {
        this.isDone = true;
    }
    public void add(PotentialStep found) {
        this.founds.add(found);
    }
    public PotentialStep getFirst() {
        return founds.isEmpty()?null:founds.get(0);
    }
    public List<PotentialStep> getFounds() {
        return founds;
    }
}