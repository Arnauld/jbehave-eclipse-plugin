package org.technbolts.jbehave.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

public class StoryParserTest {

    @Test
    public void parse_case1() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx01.story"));
        StoryParser parser = new StoryParser();

        String[] expected = {
                "offset: 0, length: 11, content: >>Narrative:\n<<", //
                "offset: 11, length: 34, content: >>In order to be more communicative\n<<", //
                "offset: 45, length: 18, content: >>As a story writer\n<<", //
                "offset: 63, length: 109, content: >>I want to explain the use of And steps and also show that I can use keywords in scenario title and comments\n\n<<", //
                "offset: 172, length: 57, content: >>Scenario: And steps should match the previous step type\n\n<<", //
                "offset: 229, length: 20, content: >>Given a 5 by 5 game\n<<", //
                "offset: 249, length: 33, content: >>When I toggle the cell at (2, 3)\n<<", //
                "offset: 282, length: 61, content: >>Then the grid should look like\n.....\n.....\n.....\n..X..\n.....\n<<", //
                "offset: 343, length: 33, content: >>When I toggle the cell at (2, 4)\n<<", //
                "offset: 376, length: 61, content: >>Then the grid should look like\n.....\n.....\n.....\n..X..\n..X..\n<<" };

        List<StoryPart> parts = parser.parse(storyAsText);
        assertParts(expected, parts);
    }

    private void assertParts(String[] expected, List<StoryPart> parts) {
        int index = 0;
        for (StoryPart b : parts) {
            String partAsString = asString(b);
            assertThat(partAsString, equalTo(expected[index++].replace("\n", "\\n")));
        }
        assertThat(index, equalTo(expected.length));
    }

    private static String asString(StoryPart b) {
        return "offset: " + b.getOffset() + ", " + "length: " + b.getLength() + ", " + "content: >>"
                + b.getContent().replace("\n", "\\n") + "<<";
    }

    @Test
    public void parse_case2() throws Exception {
        String storyAsText = "Given a user named Bob\n" + "When user credits is 12 dollars";
        StoryParser parser = new StoryParser();
        String[] expected = { "offset: 0, length: 23, content: >>Given a user named Bob\n<<", //
                "offset: 23, length: 31, content: >>When user credits is 12 dollars<<" };
        List<StoryPart> parts = parser.parse(storyAsText);
        assertParts(expected, parts);
    }

    @Test
    public void parse_case4() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx04.story"));
        StoryParser parser = new StoryParser();
        String[] expected = {
                "offset: 0, length: 219, content: >>Given a new account named 'networkAgent' with the following properties (properties not set will be completed) \n|key|value|\n|Login|networkAgentLogin|\n|Password|networkAgentPassword|\n!-- Test login using a bad password !\n<<", //
                "offset: 219, length: 31, content: >>When agent displays Login page\n<<", //
                "offset: 250, length: 95, content: >>When agent fill in the login filed 'networkAgentLogin' and in the password field 'BadPassword'\n<<", //
                "offset: 345, length: 34, content: >>When agent clicks on Login button\n<<", //
                "offset: 379, length: 87, content: >>Then agent see incorrect login error message\n!-- Test login using a correct password !\n<<", //
                "offset: 466, length: 104, content: >>When agent fill in the login filed 'networkAgentLogin' and in the password field 'networkAgentPassword'\n<<", //
                "offset: 570, length: 34, content: >>When agent clicks on Login button\n<<", //
                "offset: 604, length: 41, content: >>Then agent see the application home page\n<<" };
        List<StoryPart> parts = parser.parse(storyAsText);
        assertParts(expected, parts);
    }

    @Test
    public void parse_case5_exampleTable() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx05-exampletable.story"));
        StoryParser parser = new StoryParser();
        String[] expected = {
                "offset: 0, length: 42, content: >>Given that Larry has done <trades> trades\n<<", //
                "offset: 42, length: 87, content: >>Then the traders activity is: \n|name|trades|\n|Larry|<trades>|\n|Moe|1000|\n|Curly|2000|\n\n<<", //
                "offset: 129, length: 34, content: >>Examples:\n|<trades>|\n|3000|\n|5000|<<" };
        List<StoryPart> parts = parser.parse(storyAsText);
        assertParts(expected, parts);
    }

    @Test
    public void parse_case6_exampleTable() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx06-exampletable.story"));
        StoryParser parser = new StoryParser();
        String[] expected = {
                "offset: 0, length: 220, content: >>Given a new account named 'networkAgent' with the following properties (properties not set will be completed) \n|key|value|\n|Login|networkAgentLogin|\n|Password|networkAgentPassword|\n\n!-- Test login using a bad password !\n<<",//
                "offset: 220, length: 31, content: >>When agent displays Login page\n<<",//
                "offset: 251, length: 95, content: >>When agent fill in the login filed 'networkAgentLogin' and in the password field 'BadPassword'\n<<",//
                "offset: 346, length: 34, content: >>When agent clicks on Login button\n<<",//
                "offset: 380, length: 45, content: >>Then agent see incorrect login error message\n<<" };
        List<StoryPart> parts = parser.parse(storyAsText);
        assertParts(expected, parts);
    }

    @Test
    public void parse_case7_exampleTable() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream(
                "/data/UseCaseEx07-exampletable-comment.story"));
        StoryParser parser = new StoryParser();
        String[] expected = {
                "offset: 0, length: 199, content: >>Given a new account named 'networkAgent' with the following properties (properties not set will be completed) \n|key|value|\n!-- Some comment\n|Login|networkAgentLogin|\n|Password|networkAgentPassword|\n\n<<",//
                "offset: 199, length: 96, content: >>Examples:\n|foo|foo|\n|bar|whatever|\n|-- a comment\n|bar2|whatever|\n|-- yet another\n|bar3|whatever|<<"
        };
        List<StoryPart> parts = parser.parse(storyAsText);
        assertParts(expected, parts);
    }

}
