package org.technbolts.jbehave.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.jbehave.core.steps.StepType;
import org.junit.Before;
import org.junit.Test;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.util.ParametrizedString;
import org.technbolts.util.ParametrizedString.WeightChain;

public class ContentWithIgnorableEmitterTest {
    private static final String STEP1 = "Given an account named '$name' with the following properties:$properties";
    public static final String NL = "\n";
    
    public static final String GIVEN1 = "Given an account named 'Travis' with the following properties:" + NL +
            "|key|value|" + NL +
            "!-- Some comment" + NL + 
            "|Login|Travis|" + NL +
            "|Password|p4cm4n|" + NL;

    public static final String EXPECTED1 = "<D>Given an account named '</D><I>Travis</I><D>' with the following properties:</D><I>" + NL +
            "|key|value|" + NL + "</I>" +
            "<C>!-- Some comment" + NL + "</C>" + 
            "<I>|Login|Travis|" + NL +
            "|Password|p4cm4n|" + NL + "</I>";
    
    private StoryPart storyPart;
    private PotentialStep potentialStep;
    private ParametrizedString pString;
    private Collector collector;

    @Before
    public void setUp () {
        IMethod method = null;
        IAnnotation annotation = null;
        potentialStep = new PotentialStep(method, annotation, StepType.GIVEN, STEP1);
        pString = potentialStep.getParametrizedString();
        
        storyPart = new StoryPart(17, GIVEN1);
        collector = new Collector();
    }
    
    @Test
    public void useCase_exampleTableWithComment () {
        String rawContent = storyPart.getContent();
        
        ContentWithIgnorableEmitter emitter = new ContentWithIgnorableEmitter(Constants.commentLineMatcher, rawContent);
        String input = emitter.contentWithoutIgnorables();
        
        WeightChain chain = pString.calculateWeightChain(input);
        List<String> chainTokens = chain.tokenize();
        for(int i=0;i<chainTokens.size();i++) {
            org.technbolts.util.ParametrizedString.Token pToken = pString.getToken(i);
            // be aware that the token length can be shorter than the content length
            // because content can also contain comment :)
            String content = chainTokens.get(i);
            if(pToken.isIdentifier) {
                emitter.emitNext(pToken.offset, content.length(), collector, "I");
            }
            else {
                emitter.emitNext(pToken.offset, content.length(), collector, "D");
            }
        }
        assertThat(collector.emittedList.size(), equalTo(6));
        assertThat(collector.applyOn(rawContent, false), equalTo(rawContent));
        assertThat(collector.applyOn(rawContent, true), equalTo(EXPECTED1));
    }

    private static class Collector implements ContentWithIgnorableEmitter.Callback<String> {
        public final List<Emitted> emittedList = new ArrayList<Emitted>();
        @Override
        public void emit(String what, int offset, int length) {
            emittedList.add(new Emitted(offset, length, what));
        }
        public String applyOn(String rawContent, boolean surroundWithTokenInfo) {
            StringBuilder builder = new StringBuilder ();
            for(Emitted emitted : emittedList) {
                if(surroundWithTokenInfo) {
                    builder.append("<").append(emitted.what).append(">");
                }
                builder.append(rawContent.substring(emitted.offset, emitted.offset+emitted.length));
                if(surroundWithTokenInfo) {
                    builder.append("</").append(emitted.what).append(">");
                }
            }
            return builder.toString();
        }
        @Override
        public void emitIgnorable(int offset, int length) {
            emit("C", offset, length);
        }
    }
    
    private static class Emitted {
        public final int offset;
        public final int length;
        public final String what;
        public Emitted(int offset, int length, String what) {
            super();
            this.offset = offset;
            this.length = length;
            this.what = what;
        }
    }
    

}
