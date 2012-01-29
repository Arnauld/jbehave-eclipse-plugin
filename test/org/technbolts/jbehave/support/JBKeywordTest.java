package org.technbolts.jbehave.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.technbolts.jbehave.support.JBKeyword.*;

import java.util.Locale;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.testng.annotations.Test;

public class JBKeywordTest {

    @Test
    public void asString() {
        Keywords keywords = new LocalizedKeywords(Locale.US);
        assertThat(Meta.asString(keywords), equalTo("Meta:"));
        assertThat(MetaProperty.asString(keywords), equalTo("@"));
        assertThat(Narrative.asString(keywords), equalTo("Narrative:"));
        assertThat(InOrderTo.asString(keywords), equalTo("In order to"));
        assertThat(AsA.asString(keywords), equalTo("As a"));
        assertThat(IWantTo.asString(keywords), equalTo("I want to"));
        assertThat(Scenario.asString(keywords), equalTo("Scenario:"));
        assertThat(GivenStories.asString(keywords), equalTo("GivenStories:"));
        assertThat(ExamplesTable.asString(keywords), equalTo("Examples:"));
        assertThat(ExamplesTableRow.asString(keywords), equalTo("Example:"));
        assertThat(ExamplesTableHeaderSeparator.asString(keywords), equalTo("|"));
        assertThat(ExamplesTableValueSeparator.asString(keywords), equalTo("|"));
        assertThat(ExamplesTableIgnorableSeparator.asString(keywords), equalTo("|--"));
        assertThat(Given.asString(keywords), equalTo("Given"));
        assertThat(When.asString(keywords), equalTo("When"));
        assertThat(Then.asString(keywords), equalTo("Then"));
        assertThat(And.asString(keywords), equalTo("And"));
        assertThat(Ignorable.asString(keywords), equalTo("!--"));
    }
}
