package org.technbolts.jbehave.eclipse;

import java.util.Locale;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.technbolts.jbehave.support.JBKeyword;
import org.technbolts.util.CharTree;

public class LocalizedStepSupport {

    private CharTree<JBKeyword> kwTree;

    private Locale storyLocale;

    private LocalizedKeywords localizedKeywords;

    public void setStoryLocale(Locale storyLocale) {
        this.storyLocale = storyLocale;
        localizedKeywords = null;
        kwTree = null;
    }

    public Locale getLocale() {
        return storyLocale;
    }

    public CharTree<JBKeyword> sharedKeywordCharTree() {
        if (kwTree == null)
            kwTree = createKeywordCharTree();
        return kwTree;
    }

    public LocalizedKeywords getLocalizedKeywords() {
        if (localizedKeywords == null)
            localizedKeywords = new LocalizedKeywords(storyLocale);
        return localizedKeywords;
    }

    protected CharTree<JBKeyword> createKeywordCharTree() {
        LocalizedKeywords keywords = getLocalizedKeywords();
        CharTree<JBKeyword> cn = new CharTree<JBKeyword>('/', null);
        for (JBKeyword kw : JBKeyword.values()) {
            String asString = kw.asString(keywords);
            cn.push(asString, kw);
        }
        return cn;
    }

    public String lGiven(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().given(), withTrailingSpace);
    }

    public String lAnd(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().and(), withTrailingSpace);
    }

    public String lAsA(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().asA(), withTrailingSpace);
    }

    public String lExamplesTable(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().examplesTable(), withTrailingSpace);
    }

    public String lGivenStories(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().givenStories(), withTrailingSpace);
    }

    public String lIgnorable(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().ignorable(), withTrailingSpace);
    }

    public String lInOrderTo(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().inOrderTo(), withTrailingSpace);
    }

    public String lIWantTo(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().iWantTo(), withTrailingSpace);
    }

    public String lMeta(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().meta(), withTrailingSpace);
    }

    public String lNarrative(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().narrative(), withTrailingSpace);
    }

    public String lScenario(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().scenario(), withTrailingSpace);
    }

    public String lThen(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().then(), withTrailingSpace);
    }

    public String lWhen(boolean withTrailingSpace) {
        return plusSpace(getLocalizedKeywords().when(), withTrailingSpace);
    }

    private static String plusSpace(String aString, boolean wantSpace) {
        return wantSpace ? aString + " " : aString;
    }

}
