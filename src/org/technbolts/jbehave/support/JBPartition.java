package org.technbolts.jbehave.support;

import java.util.List;

import org.technbolts.util.New;

public enum JBPartition {

    Step,
    Narrative,
    ExampleTable,
    Comment,
    Scenario,
    Misc,
    Undefined;
    
    public static boolean arePartitionsEqual(JBKeyword keyword1, JBKeyword keyword2) {
        return partitionOf(keyword1)==partitionOf(keyword2);
    }
    
    public static List<String> names () {
        List<String> types = New.arrayList();
        for(JBPartition partition : JBPartition.values())
            types.add(partition.name());
        return types;
    }
    
    public static JBPartition partitionOf(JBKeyword keyword) {
        switch(keyword) {
            case Given:
            case When:
            case Then:
            case And : 
                return Step;
            case ExamplesTable:
            case ExamplesTableHeaderSeparator:
            case ExamplesTableIgnorableSeparator:
            case ExamplesTableValueSeparator:
            case ExamplesTableRow:
                return ExampleTable;
            case Ignorable:
                return Comment;
            case Narrative:
            case AsA:
            case InOrderTo:
            case IWantTo:
                return Narrative;
            case Scenario:
                return Scenario;
            case GivenStories: 
            case Meta: 
            case MetaProperty:
                return Misc;
        }
        return Undefined;
    }
}
