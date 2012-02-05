package org.technbolts.eclipse.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;
import org.technbolts.util.Strings;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HtmlStateMachine {
    
    private StringBuilder buffer = new StringBuilder ();
    private Map<Tag,Int> counters = init();
    private StyledString styledString = new StyledString();

    public void parse(String content) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new ByteArrayInputStream(content.getBytes("utf-8")), new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                buffer.append(ch, start, length);
            }
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
                startTag(qName);
            }
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                endTag(qName);
            }
        });
    }
    
    private void emit() {
        if(buffer.length()==0)
            return;
        
        System.out.println(">>> bold:" + isBold() + ", isItalic: " + isItalic() + ", isCode: " + isCode() + ", isInLi: " + isInLi());
        System.out.println(buffer);
        System.out.println("<<<");
        Styler styler = currentStyler();
        styledString.append(buffer.toString(), styler);
        
        buffer.setLength(0);
    }
    
    private Styler currentStyler() {
        Styler styler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                
            }
        };
        return null;
    }

    private String NL = "\\n";
    
    private void emitNL() {
        System.out.println(NL);
    }
    
    private void rawEmit(String what) {
        System.out.println(Strings.times(get(Tag.li), " ") + what);
    }

    protected boolean isBold() {
        return inTag(Tag.b);
    }
    
    protected boolean isItalic() {
        return inTag(Tag.i);
    }
    
    protected boolean isCode() {
        return inTag(Tag.code);
    }
    
    protected boolean isInLi() {
        return inTag(Tag.li);
    }
    
    private boolean inTag(Tag tag) {
        return get(tag)>0;
    }

    private int get(Tag tag) {
        return getInt(tag).value;
    }

    private Int getInt(Tag tag) {
        return counters.get(tag);
    }

    private static HashMap<Tag, Int> init() {
        HashMap<Tag, Int> map = new HashMap<Tag, Int>();
        for(Tag tag : Tag.values()) {
            map.put(tag, new Int());
        }
        return map;
    }
    
    private void startTag(String localName) {
        if("root".equals(localName))
            return;
        emit();
        Tag tag = Tag.tagOf(localName);
        getInt(tag).value++;
        switch(tag) {
            case br:
            case ul:
            case p:
                emitNL();
                break;
            case li:
                emitNL();
                rawEmit("-");
                break;
        }
    }
    
    private void endTag(String localName) {
        emit();
        if("root".equals(localName))
            return;
        
        Tag tag = Tag.tagOf(localName);
        getInt(tag).value--;
    }
    
    private static class Int {
        int value = 0;
    }
    
    enum Tag {
        b(),
        i(),
        em(i),
        strong(b),
        code(),
        tt(code),
        anchor(),
        p(),
        ul(),
        li(),
        br();
        
        public final Tag suggested;
        private Tag() {
            this.suggested = this;
        }
        private Tag(Tag suggested) {
            this.suggested = suggested;
        }
        public static Tag tagOf(String n) {
            for(Tag tag : values()) {
                if(tag.name().equals(n))
                    return tag.suggested;
            }
            throw new IllegalArgumentException("Unknown tag <" + n + ">");
        }
    }

}
