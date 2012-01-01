package org.technbolts.jbehave.eclipse.textstyle;

import java.util.Map;

import org.eclipse.swt.graphics.RGB;

public class TextStyleTheme {
    public static TextStyle createDarkTheme () {
        TextStyle root = new TextStyleTreeBuilder().createTree("dark");
        Map<String,TextStyle> map = root.createMap(); 
        define(map, "default", new RGB(0,0,0),new RGB(255,255,255),false,false);
        define(map, "narrative_default", null,new RGB(238,159,97),true,false);
        define(map, "narrative_keyword", null,null,false,true);
        define(map, "comment", null,new RGB(210,255,210),false,false);
        define(map, "meta_default", null,new RGB(232,178,255),false,false);
        define(map, "meta_keyword", null,null,false,true);
        define(map, "scenario_default", null,new RGB(255,237,117),true,false);
        define(map, "scenario_keyword", null,null,false,true);
        define(map, "step_default", null,new RGB(223,225,225),false,false);
        define(map, "step_keyword", null,new RGB(118,197,255),false,true);
        define(map, "step_parameter", null,new RGB(192,230,249),true,true);
        define(map, "step_parameter_value", null,new RGB(209,235,253),true,false);
        define(map, "step_example_table_separator", null,new RGB(255,169,249),false,false);
        define(map, "step_example_table_cell", null,new RGB(190,248,255),true,false);
        define(map, "example_table_default", null,new RGB(223,225,225),false,false);
        define(map, "example_table_keyword", null,new RGB(118,197,255),false,true);
        define(map, "example_table_separator", null,new RGB(255,169,249),false,false);
        define(map, "example_table_cell", null,new RGB(190,248,255),true,false);
        return root;
    }
    
    public static TextStyle createLightTheme() {
        TextStyle root = new TextStyleTreeBuilder().createTree("light");
        Map<String,TextStyle> map = root.createMap(); 
        define(map, "default", new RGB(255,255,255),new RGB(0,0,0),false,false);
        define(map, "narrative_default", null,new RGB(183,57,20),true,false);
        define(map, "narrative_keyword", null,null,false,true);
        define(map, "comment", null,new RGB(62,165,0),false,false);
        define(map, "meta_default", null,new RGB(156,5,203),false,false);
        define(map, "meta_keyword", null,null,false,true);
        define(map, "scenario_default", null,new RGB(203,95,0),true,false);
        define(map, "scenario_keyword", null,null,false,true);
        define(map, "step_default", null,new RGB(81, 37, 16),false,false);
        define(map, "step_keyword", null,new RGB(81, 37, 16),false,true);
        define(map, "step_parameter", null,new RGB(183,57,20),true,true);
        define(map, "step_parameter_value", null,new RGB(183,57,20),true,false);
        define(map, "step_example_table_separator", null,new RGB(205,131,55),false,false);
        define(map, "step_example_table_cell", null,new RGB(183,57,20),true,false);
        define(map, "example_table_default", null,null,false,false);
        define(map, "example_table_keyword", null,new RGB(144,144,144),false,true);
        define(map, "example_table_separator", null,new RGB(205,131,55),false,false);
        define(map, "example_table_cell", null,new RGB(183,57,20),true,false);
        return root;
    }

    public static void define(Map<String, TextStyle> map, String key, RGB background, RGB foreground, boolean italic, boolean bold) {
        TextStyle style = map.get(key);
        style.setBackground(background);
        style.setForeground(foreground);
        style.setItalic(italic);
        style.setBold(bold);
    }
}
