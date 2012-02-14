package org.technbolts.jbehave.eclipse;

import java.util.Locale;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.CharTree;

public interface LocalizedStepSupport {

    Locale getLocale();

    CharTree<JBKeyword> sharedKeywordCharTree();

    LocalizedKeywords getLocalizedKeywords();

    String lGiven(boolean withTrailingSpace);

    String lAnd(boolean withTrailingSpace);

    String lAsA(boolean withTrailingSpace);

    String lExamplesTable(boolean withTrailingSpace);

    String lGivenStories(boolean withTrailingSpace);

    String lIgnorable(boolean withTrailingSpace);

    String lInOrderTo(boolean withTrailingSpace);

    String lIWantTo(boolean withTrailingSpace);

    String lMeta(boolean withTrailingSpace);

    String lNarrative(boolean withTrailingSpace);

    String lScenario(boolean withTrailingSpace);

    String lThen(boolean withTrailingSpace);

    String lWhen(boolean withTrailingSpace);

}