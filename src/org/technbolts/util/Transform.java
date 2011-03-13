package org.technbolts.util;

public interface Transform<R,T> {
    T transform(R elem);
}
