package org.technbolts.jbehave.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.jbehave.core.steps.StepType;
import org.junit.Before;
import org.junit.Test;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.parser.Constants.TokenizerCallback;
import org.technbolts.util.ParametrizedString;
import org.technbolts.util.ParametrizedString.WeightChain;

public class InsertThroughCommentTest {
    
    public static final String NL = "\n";
    public static final String S1 = "Given an account named 'Travis' with the following properties" + NL +
            "|key|value|" + NL +
            "!-- Some comment" + NL + 
            "|Login|Travis|" + NL +
            "|Password|p4cm4n|" + NL;
    
    private StoryPart storyPart;
    private PotentialStep potentialStep;
    private ParametrizedString pString;

    @Before
    public void setUp () {
        IMethod method = null;
        IAnnotation annotation = null;
        potentialStep = new PotentialStep(method, annotation, StepType.GIVEN, "Given an account named '$name' with the following properties $properties");
        pString = potentialStep.getParametrizedString();
        
        storyPart = new StoryPart(17, S1);
    }
    
    @Test
    public void withinStepExampleTable () {
        Sink sink = new Sink() {
            @Override
            public void emit(String tokId, int offset, int length) {
                System.out.println("[" + offset + ", " + (offset + length) + "] " + tokId + "");
            }
        };
        
        IgnorableAware ignorable = new IgnorableAware(Constants.commentLineMatcher, storyPart.getContent());
        String input = ignorable.cleanedContent();
        WeightChain chain = pString.calculateWeightChain(input);
        List<String> chainTokens = chain.tokenize();
        for(int i=0;i<chainTokens.size();i++) {
            org.technbolts.util.ParametrizedString.Token pToken = pString.getToken(i);
            String content = chainTokens.get(i);
            
            if(pToken.isIdentifier) {
                ignorable.emitNext(pToken.offset, "I", content, sink);
            }
            else {
                ignorable.emitNext(pToken.offset, "D", content, sink);
            }
        }
    }
    
    public static class Fragment {
        public final int startOffset;
        public final int endOffset;
        public final boolean isIgnorable;
        public int remaining;
        public Fragment(int startOffset, int endOffset, boolean isIgnorable) {
            super();
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.remaining = isIgnorable?0:length();
            this.isIgnorable = isIgnorable;
        }
        public int length() {
            return endOffset-startOffset;
        }
        public boolean contains(int offset) {
            return startOffset<=offset && offset<endOffset;
        }
        public boolean hasRemaining() {
            return remaining>0;
        }
        public int consume(int length) {
            if(isIgnorable)
                return 0;
            if(length>remaining) {
                int consumed = remaining;
                remaining = 0;
                return consumed;
            }
            remaining -= length;
            return length;
        }
    }
    
    public interface Sink {
        void emit(String tokId, int offset, int length);
    }
    
    public static class IgnorableAware {
        private List<Fragment> fragments;
        private int accumulatedDelta = 0;
        private int lastFragmentUsed = 0;
        private String content;
        public IgnorableAware(Pattern ignorablePattern, String content) {
            this.content = content;
            this.fragments = generateFragments(ignorablePattern, content);
        }
        
        public String cleanedContent() {
            StringBuilder builder = new StringBuilder();
            for(Fragment f : fragments) {
                if(f.isIgnorable)
                    continue;
                builder.append(content.substring(f.startOffset, f.endOffset));
            }
            return builder.toString();
        }
        
        public void emitNext(int offsetInIgnoredSpace, String tokId, final String contentInIgnoredSpace, Sink sink) {
            int contentToEmit = contentInIgnoredSpace.length();
            for(int i=lastFragmentUsed; i<fragments.size(); i++) {
                Fragment f = fragments.get(i);
                if(f.isIgnorable) {
                    sink.emit("C", accumulatedDelta, f.length());
                    accumulatedDelta += f.length();
                    continue;
                }
                int consumed = f.consume(contentToEmit);
                if(consumed>0) {
                    sink.emit(tokId, accumulatedDelta, consumed);
                    accumulatedDelta += consumed;
                }
                contentToEmit -= consumed;
                if(contentToEmit==0)
                    return;
                lastFragmentUsed++;
            }
            sink.emit(tokId, accumulatedDelta+offsetInIgnoredSpace, contentToEmit);
            accumulatedDelta += contentInIgnoredSpace.length();
        }
    }

    public static List<Fragment> generateFragments(Pattern ignorablePattern, String content) {
        final List<Fragment> fragments = new ArrayList<Fragment>();
        Constants.tokenize(ignorablePattern, content, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String token, boolean isDelimiter) {
                fragments.add(new Fragment(startOffset, endOffset, isDelimiter));
            }
        });
        return fragments;
    }
}
