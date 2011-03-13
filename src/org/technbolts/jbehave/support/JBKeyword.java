package org.technbolts.jbehave.support;

import org.jbehave.core.configuration.Keywords;
import org.technbolts.util.Filter;

public enum JBKeyword {
    Meta {
        @Override
        public String asString(Keywords keywords) {
            return keywords.meta();
        }
    },
    MetaProperty {
        @Override
        public String asString(Keywords keywords) {
            return keywords.metaProperty();
        }
    },
    Narrative {
        @Override
        public String asString(Keywords keywords) {
            return keywords.narrative();
        }
    },
    InOrderTo {
        @Override
        public String asString(Keywords keywords) {
            return keywords.inOrderTo();
        }
    },
    AsA {
        @Override
        public String asString(Keywords keywords) {
            return keywords.asA();
        }
    },
    IWantTo {
        @Override
        public String asString(Keywords keywords) {
            return keywords.iWantTo();
        }
    },
    Scenario {
        @Override
        public String asString(Keywords keywords) {
            return keywords.scenario();
        }
    },
    GivenStories {
        @Override
        public String asString(Keywords keywords) {
            return keywords.givenStories();
        }
    },
    ExamplesTable {
        @Override
        public String asString(Keywords keywords) {
            return keywords.examplesTable();
        }
    },
    ExamplesTableRow {
        @Override
        public String asString(Keywords keywords) {
            return keywords.examplesTableRow();
        }
    },
    ExamplesTableHeaderSeparator {
        @Override
        public String asString(Keywords keywords) {
            return keywords.examplesTableHeaderSeparator();
        }
    },
    ExamplesTableValueSeparator {
        @Override
        public String asString(Keywords keywords) {
            return keywords.examplesTableValueSeparator();
        }
    },
    ExamplesTableIgnorableSeparator{
        @Override
        public String asString(Keywords keywords) {
            return keywords.examplesTableIgnorableSeparator();
        }
    },
    Given {
        @Override
        public String asString(Keywords keywords) {
            return keywords.given();
        }
    },
    When {
        @Override
        public String asString(Keywords keywords) {
            return keywords.when();
        }
    },
    Then {
        @Override
        public String asString(Keywords keywords) {
            return keywords.then();
        }
    },
    And {
        @Override
        public String asString(Keywords keywords) {
            return keywords.and();
        }
    },
    Ignorable {
        @Override
        public String asString(Keywords keywords) {
            return keywords.ignorable();
        }
    };
    
    public String asString(Keywords keywords) {
        throw new AbstractMethodError();
    }
    
    public static JBKeyword lookup(StringBuilder builder, Keywords keywords) {
        int len = builder.length();
        for(JBKeyword jk : values()) {
            String asString = jk.asString(keywords);
            if(asString.length()!=builder.length()) {
                continue;
            }
            
            boolean match = true;
            for(int i=0;i<len && match;i++) {
                if(builder.charAt(i)!=asString.charAt(i))
                    match = false;
            }
            if(match) {
                return jk;
            }
        }
        return null;
    }
    
    public static Filter<JBKeyword> stepFilter() {
        return new Filter<JBKeyword>() {
            @Override
            public boolean isAccepted(JBKeyword keyword) {
                return isStep(keyword);
            }
        };
    }

    public static boolean isStep(JBKeyword keyword) {
        if(keyword==null)
            return false;
        switch(keyword) {
            case Given:
            case When:
            case Then:
            case And:
                return true;
            default:
                return false;
        }
    }

}