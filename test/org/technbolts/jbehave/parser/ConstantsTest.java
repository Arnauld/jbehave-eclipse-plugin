package org.technbolts.jbehave.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.technbolts.jbehave.parser.Constants.containsExampleTable;
import static org.technbolts.jbehave.parser.Constants.removeComment;

import org.junit.Test;
import org.technbolts.jbehave.parser.Constants.TokenizerCallback;

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
        Constants.splitLine(content, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String fragment, boolean isNL) {
                System.out.println(
                        "startOffset: " + startOffset+ 
                        ", endOffset: " + endOffset + 
                        ", fragment: "  + escapeNL(fragment) + 
                        ", isNL: " + isNL);
                System.out.println(">" + escapeNL(content.substring(startOffset, endOffset)) +"<");
            }
        });
    }
    
    @Test
    public void splitLine_startsWithNL() {
        final String content = NL + "Given ac account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "!-- Some comment" + NL + 
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        Constants.splitLine(content, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String fragment, boolean isNL) {
                System.out.println(
                        "startOffset: " + startOffset+ 
                        ", endOffset: " + endOffset + 
                        ", fragment: "  + escapeNL(fragment) + 
                        ", isNL: " + isNL);
                System.out.println(">" + escapeNL(content.substring(startOffset, endOffset)) +"<");
            }
        });
    }

    protected static String escapeNL(String string) {
        string = string.replace("\n", "\\n");
        string = string.replace("\r", "\\r");
        return string;
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
}
