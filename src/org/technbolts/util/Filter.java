package org.technbolts.util;

public interface Filter<T> {
    boolean isAccepted(T value);
}
