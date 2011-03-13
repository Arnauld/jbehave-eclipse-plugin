package org.technbolts.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

public class New {

    public static <T> ArrayList<T> arrayList () {
        return new ArrayList<T>();
    }
    
    public static <T> Vector<T> vector() {
        return new Vector<T>();
    }
    
    public static <K,V> HashMap<K, V> hashMap() {
        return new HashMap<K, V>();
    }

    public static <T> LinkedList<T> linkedList() {
        return new LinkedList<T>();
    }

}
