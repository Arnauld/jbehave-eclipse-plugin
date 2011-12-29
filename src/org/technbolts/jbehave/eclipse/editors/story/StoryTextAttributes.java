package org.technbolts.jbehave.eclipse.editors.story;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.technbolts.eclipse.util.TextAttributeProvider.Entry;

public class StoryTextAttributes {
    public static final RGB defaultBackground = new RGB(240, 240, 240);
    public static final RGB narrativeBackground = new RGB(255, 240, 240);
    public static final RGB exampleTableBackground = new RGB(240, 227, 200);
    
    public static final Entry Default/*~~~~~~~~~~~~~~~*/ = new Entry("story-default", new RGB(0, 0, 0), defaultBackground, SWT.NORMAL);
    public static final Entry Comment/*~~~~~~~~~~~~~~~*/ = new Entry("story-comment", new RGB(63, 125, 93), new RGB(180, 240, 180), SWT.ITALIC);
    public static final Entry Narrative/*~~~~~~~~~~~~~*/ = new Entry("story-narrative", new RGB(140, 17, 31), narrativeBackground, SWT.ITALIC);
    public static final Entry NarrativeKeyword/*~~~~~~*/ = new Entry("story-narrative-kw", new RGB(140, 17, 31), narrativeBackground, SWT.BOLD);
    public static final Entry Step/*~~~~~~~~~~~~~~~~~~*/ = new Entry("story-step", new RGB(81, 37, 16), defaultBackground, SWT.NORMAL);
    public static final Entry StepKeyword/*~~~~~~~~~~~*/ = new Entry("story-step-kw", new RGB(81, 37, 16), defaultBackground, SWT.ITALIC + SWT.BOLD);
    public static final Entry StepParameter/*~~~~~~~~~*/ = new Entry("story-step-param", new RGB(212, 32, 16), defaultBackground, SWT.ITALIC + SWT.BOLD);
    public static final Entry StepParameterValue/*~~~~*/ = new Entry("story-step-param-value", new RGB(212, 32, 16), defaultBackground, SWT.ITALIC);
    public static final Entry Scenario/*~~~~~~~~~~~~~~*/ = new Entry("story-scenario", new RGB(224, 139, 27), defaultBackground, SWT.ITALIC);
    public static final Entry ScenarioKeyword/*~~~~~~~*/ = new Entry("story-scenario-kw", new RGB(224, 139, 27), defaultBackground, SWT.BOLD);
    public static final Entry StepExampleTableSep/*~~~*/ = new Entry("story-step-table-sep", new RGB(170, 140, 120), exampleTableBackground, SWT.BOLD);
    public static final Entry StepExampleTableCell/*~~*/ = new Entry("story-step-table-cell", new RGB(212, 32, 16), exampleTableBackground, SWT.ITALIC);
}
