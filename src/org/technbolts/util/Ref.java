package org.technbolts.util;


public class Ref<T> {
    private T value;
    public void set(T value) {
        this.value = value;
    }
    public T get() {
        return value;
    }
    public static <T> Ref<T> create() {
        return new Ref<T>();
    }
    public boolean isNull() {
        return value==null;
    }
    public T getOrElse(T defaultValue) {
        if(isNull())
            return defaultValue;
        else
            return value;
    }
}
