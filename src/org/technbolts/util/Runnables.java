package org.technbolts.util;

public class Runnables {

    public static Runnable noop() {
        return new Runnable() {
            public void run() {
            }
        };
    }
}
