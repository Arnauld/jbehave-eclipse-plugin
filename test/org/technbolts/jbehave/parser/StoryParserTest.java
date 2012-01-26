package org.technbolts.jbehave.parser;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class StoryParserTest {

    @Test 
    public void parse_case1() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx01.story"));
        StoryParser parser = new StoryParser();
        for(StoryPart b : parser.parse(storyAsText)) {
            System.out.println(
                    "offset: " + b.getOffset() + ", " +
                    "length: " + b.getLength() + ", " +
                    "content: >>" + b.getContent().replace("\n", "\\n") + "<<");
        }
    }
    
    @Test
    public void parse_case2() throws Exception {
        String storyAsText = "Given a user named Bob\n" +
                "When user credits is 12 dollars";
        StoryParser parser = new StoryParser();
        for(StoryPart b : parser.parse(storyAsText)) {
            System.out.println(
                    "offset: " + b.getOffset() + ", " +
                    "length: " + b.getLength() + ", " +
                    "content: >>" + b.getContent().replace("\n", "\\n") + "<<");
        }
    }
    
    @Test 
    public void parse_case4() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx04.story"));
        StoryParser parser = new StoryParser();
        for(StoryPart b : parser.parse(storyAsText)) {
            System.out.println(
                    "offset: " + b.getOffset() + ", " +
                    "length: " + b.getLength() + ", " +
                    "content: >>" + b.getContent().replace("\n", "\\n") + "<<");
        }
    }
    
    @Test 
    public void parse_case5_exampleTable() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx05-exampletable.story"));
        StoryParser parser = new StoryParser();
        for(StoryPart b : parser.parse(storyAsText)) {
            System.out.println(
                    "offset: " + b.getOffset() + ", " +
                    "length: " + b.getLength() + ", " +
                    "content: >>" + b.getContent().replace("\n", "\\n") + "<<");
        }
    }
    
    @Test 
    public void parse_case6_exampleTable() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx06-exampletable.story"));
        StoryParser parser = new StoryParser();
        for(StoryPart b : parser.parse(storyAsText)) {
            System.out.println(
                    "offset: " + b.getOffset() + ", " +
                    "length: " + b.getLength() + ", " +
                    "content: >>" + b.getContent().replace("\n", "\\n") + "<<");
        }
    }
    
    @Test 
    public void parse_case7_exampleTable() throws IOException {
        String storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx07-exampletable-comment.story"));
        StoryParser parser = new StoryParser();
        for(StoryPart b : parser.parse(storyAsText)) {
            System.out.println(
                    "offset: " + b.getOffset() + ", " +
                    "length: " + b.getLength() + ", " +
                    "content: >>" + b.getContent().replace("\n", "\\n") + "<<");
        }
    }
    
}
