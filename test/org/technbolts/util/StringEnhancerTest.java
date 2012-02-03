package org.technbolts.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.technbolts.util.StringEnhancer.enhanceString;

import org.testng.annotations.Test;

public class StringEnhancerTest {

    @Test
    public void isTheStartOfOneOf () {
        assertThat(enhanceString("giv").isTheStartIgnoringCaseOfOneOf("and", "when", "given", "then"), is(true));
        assertThat(enhanceString("gIv").isTheStartIgnoringCaseOfOneOf("and", "when", "given", "then"), is(true));
        assertThat(enhanceString("gav").isTheStartIgnoringCaseOfOneOf("and", "when", "given", "then"), is(false));
        assertThat(enhanceString("givening").isTheStartIgnoringCaseOfOneOf("and", "when", "given", "then"), is(false));
    }
}
