package com.spaxxv.cardsmp.util;

import java.util.HashSet;
import java.util.Set;

public class SetBuilder<T> {
    private final HashSet<T> set;
    public SetBuilder() {
        set = new HashSet<>();
    }
    public SetBuilder<T> addElement(T t) {
        set.add(t);
        return this;
    }
    @SafeVarargs
    public final SetBuilder<T> addElements(T... t) {
        for (T t1 : t) addElement(t1);
        return this;
    }
    public Set<T> toSet() {
        return set;
    }
}
