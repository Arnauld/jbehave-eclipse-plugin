package org.technbolts.util;

import fj.F;
import fj.F2;
import fj.data.Array;

public class FJ {

    public static <T> int count(Iterable<T> values, final F<T, Boolean> filter) {
        return Array.iterableArray(values).foldLeft(new F2<Integer, T, Integer>() {
            public Integer f(Integer acc, T value) {
                if (filter.f(value))
                    return acc + 1;
                else
                    return acc;
            };
        }, 0);
    }

    public static <T, R> Array<R> mapAndFilter(Iterable<T> values, final F<T, R> map, final F<R, Boolean> filter) {
        return Array.iterableArray(values).map(map).filter(filter);
    }

    public static <T> F<T, Boolean> alwaysTrue() {
        return new F<T, Boolean>() {
            @Override
            public Boolean f(T arg) {
                return Boolean.TRUE;
            }
        };
    }

    public static <T> F<T, Boolean> and(final F<T, Boolean> one, final F<T, Boolean> two) {
        return new F<T, Boolean>() {
            public Boolean f(T value) {
                return one.f(value) && two.f(value);
            }
        };
    }

}
