package org.technbolts.jbehave.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.technbolts.jbehave.parser.Constants.containsExampleTable;
import static org.technbolts.jbehave.parser.Constants.*;

import java.util.List;

import org.technbolts.jbehave.parser.Constants.TokenizerCallback;
import org.technbolts.util.New;
import org.testng.annotations.Test;

public class ConstantsTest {
    
    private static String NL = "\n";
    
    @Test
    public void containsExampleTable_noTable() {
        String content = "Given ac account named 'networkAgent' with the following properties";
        assertThat(containsExampleTable(content), is(false));
    }
    
    @Test
    public void containsExampleTable_noTableButComment() {
        String content = "Given ac account named 'networkAgent' with the following properties" + NL +
                         "!-- Some comment" + NL;
        assertThat(containsExampleTable(content), is(false));
    }

    @Test
    public void containsExampleTable_withTable() {
        String content = "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }
    
    @Test
    public void containsExampleTable_withTableAndComment() {
        String content = "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "!-- Some comment" + NL + 
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }
    
    @Test
    public void containsExampleTable_withTable_edgeCase1() {
        String content = "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|-|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }

    @Test
    public void containsExampleTable_withTable_edgeCase2() {
        String content = "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|-a-|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }
    
    @Test
    public void splitLine() {
        final String content = "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "!-- Some comment" + NL + 
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        TokenCollector collector = new TokenCollector();
        Constants.splitLine(content,collector);
        List<String> tokens = collector.getTokens();
        assertThat(tokens.size(), equalTo(10));
        assertThat(tokens.get(0), equalTo("Given ac account named 'networkAgent' with the following properties"));
        assertThat(tokens.get(1), equalTo(NL));
        assertThat(tokens.get(2), equalTo("|key|value|"));
        assertThat(tokens.get(3), equalTo(NL));
        assertThat(tokens.get(4), equalTo("!-- Some comment"));
        assertThat(tokens.get(5), equalTo(NL));
        assertThat(tokens.get(6), equalTo("|Login|networkAgentLogin|"));
        assertThat(tokens.get(7), equalTo(NL));
        assertThat(tokens.get(8), equalTo("|Password|networkAgentPassword|"));
        assertThat(tokens.get(9), equalTo(NL));
    }
    
    @Test
    public void splitLine_startsWithNL() {
        final String content = NL + "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "!-- Some comment" + NL + 
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        
        TokenCollector collector = new TokenCollector();
        Constants.splitLine(content,collector);
        List<String> tokens = collector.getTokens();
        assertThat(tokens.size(), equalTo(11));
        assertThat(tokens.get(0), equalTo(NL));
        assertThat(tokens.get(1), equalTo("Given ac account named 'networkAgent' with the following properties"));
        assertThat(tokens.get(2), equalTo(NL));
        assertThat(tokens.get(3), equalTo("|key|value|"));
        assertThat(tokens.get(4), equalTo(NL));
        assertThat(tokens.get(5), equalTo("!-- Some comment"));
        assertThat(tokens.get(6), equalTo(NL));
        assertThat(tokens.get(7), equalTo("|Login|networkAgentLogin|"));
        assertThat(tokens.get(8), equalTo(NL));
        assertThat(tokens.get(9), equalTo("|Password|networkAgentPassword|"));
        assertThat(tokens.get(10), equalTo(NL));
    }
    
    @Test
    public void removeComment_noComment () {
        String input = "Given ac account named 'networkAgent'";
        assertThat(removeComment(input), equalTo(input));
    }
    
    @Test
    public void removeComment_onlyOneComment () {
        assertThat(removeComment("!-- Some comment"), equalTo(""));
    }
    
    @Test
    public void removeComment_onlySeveralComments () {
        assertThat(removeComment(
                "!-- Some comment" + NL + 
                "!-- Some other comment" + NL), equalTo(""));
        assertThat(removeComment(
                "!-- Some comment" + NL + 
                "!-- Some other comment"), equalTo(""));
    }
    
    @Test
    public void removeComment_withNLAndEndingWithComment () {
        assertThat(removeComment(
                "Given ac account named 'networkAgent' with the following properties" + NL + 
                NL + 
                "!-- Some other comment" + NL), 
                equalTo("Given ac account named 'networkAgent' with the following properties" + NL));
    }
    
    @Test
    public void removeComment_ex1 () {
        final String actual = NL + "Given ac account named 'networkAgent' with the following properties" + NL +
                        "|key|value|" + NL +
                        "!-- Some comment" + NL + 
                        "|Login|networkAgentLogin|" + NL +
                        "|Password|networkAgentPassword|" + NL;
        final String expected = NL + "Given ac account named 'networkAgent' with the following properties" + NL +
                        "|key|value|" + NL +
                        "|Login|networkAgentLogin|" + NL +
                        "|Password|networkAgentPassword|" + NL;
        assertThat(removeComment(actual), equalTo(expected));
    }
    
    @Test
    public void removeTrailingComment_noComment () {
        final String actual = NL + "Given an account named 'networkAgent'" + NL;
        final String expected = NL + "Given an account named 'networkAgent'" + NL;
        assertThat(removeTrailingComment(actual), equalTo(expected));
    }
    
    @Test
    public void removeTrailingComment_ex1 () {
        final String actual = NL + "Given an account named 'networkAgent'" + NL +
                NL + 
                "!-- Some comment" + NL;
        final String expected = NL + "Given an account named 'networkAgent'" + NL;
        assertThat(removeTrailingComment(actual), equalTo(expected));
    }
    
    @Test
    public void tokenize_() {
        final String actual = NL + "Given ac account named 'networkAgent' with the following properties" + NL +
                        "|key|value|" + NL +
                        "!-- Some comment" + NL + 
                        "|Login|networkAgentLogin|" + NL +
                        "|Password|networkAgentPassword|" + NL;

        TokenCollector collector = new TokenCollector();
        Constants.tokenize(Constants.commentLineMatcher, actual, collector);
        List<String> tokens = collector.getTokens();
        assertThat(tokens.size(), equalTo(3));
        assertThat(tokens.get(0), equalTo(NL + "Given ac account named 'networkAgent' with the following properties" + NL +
                "|key|value|" + NL));
        assertThat(tokens.get(1), equalTo("!-- Some comment" + NL));
        assertThat(tokens.get(2), equalTo("|Login|networkAgentLogin|" + NL +
                "|Password|networkAgentPassword|" + NL));
    }
    
    private static class TokenCollector implements TokenizerCallback {
        private List<String> tokens = New.arrayList();
        @Override
        public void token(int startOffset, int endOffset, String token, boolean isDelimiter) {
            tokens.add(token);
        }
        public List<String> getTokens() {
            return tokens;
        }
    }
}
