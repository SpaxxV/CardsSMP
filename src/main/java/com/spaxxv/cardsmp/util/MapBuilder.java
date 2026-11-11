package com.spaxxv.cardsmp.util;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K,V> {
    private final HashMap<K,V> map;
    public MapBuilder() {
        map = new HashMap<>();
    }
    public MapBuilder<K,V> addElement(K k, V v) {
        map.put(k, v);
        return this;
    }
    public Map<K,V> toMap() {
        return map;
    }
}
