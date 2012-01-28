package org.technbolts.jbehave.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.technbolts.jbehave.parser.Constants.TokenizerCallback;

public class ContentWithIgnorableEmitter {
    
    public interface Callback<T> {
        void emit(T arg, int offset, int length);
        void emitIgnorable(int offset, int length);
    }

    private List<Fragment> fragments;
    private int accumulatedDelta = 0;
    private int lastFragmentUsed = 0;
    private String content;
    public ContentWithIgnorableEmitter(Pattern ignorablePattern, String content) {
        this.content = content;
        this.fragments = generateFragments(ignorablePattern, content);
    }
    
    public List<Fragment> getFragments() {
        return fragments;
    }
    
    public String contentWithoutIgnorables() {
        StringBuilder builder = new StringBuilder();
        for(Fragment f : fragments) {
            if(!f.isIgnorable) {
                builder.append(content.substring(f.startOffset, f.endOffset));
            }
        }
        return builder.toString();
    }
    
    public <T> void emitNext(int offsetInIgnoredSpace, int length, Callback<T> sink, T arg) {
        int contentToEmit = length;
        for(int i=lastFragmentUsed; i<fragments.size(); i++) {
            Fragment f = fragments.get(i);
            if(f.isIgnorable) {
                sink.emitIgnorable(accumulatedDelta, f.length());
                accumulatedDelta += f.length();
                continue;
            }
            int consumed = f.consume(contentToEmit);
            if(consumed>0) {
                sink.emit(arg, accumulatedDelta, consumed);
                accumulatedDelta += consumed;
            }
            contentToEmit -= consumed;
            if(contentToEmit==0)
                return;
            lastFragmentUsed++;
        }
        
        // remaining?
        if(contentToEmit>0)
            sink.emit(arg, accumulatedDelta+offsetInIgnoredSpace, contentToEmit);
        accumulatedDelta += contentToEmit;
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
        
        @Override
        public String toString() {
            return "Fragment [startOffset=" + startOffset + ", endOffset=" + endOffset + ", isIgnorable=" + isIgnorable
                    + ", remaining=" + remaining + "]";
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

}


