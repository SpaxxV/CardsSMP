package com.spaxxv.cardsmp.data.values;


import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.DataUtils;

import java.nio.ByteBuffer;

public class DataSubMap extends DataElement {
    public DataMap value;
    public DataSubMap() {
        this(new DataMap());
    }
    public DataSubMap(DataMap m) {
        value = m;
    }
    public DataElement get(String key) {
        return value.get(key);
    }
    public DataSubMap set(String key, DataElement val) {
        value.set(key, val);
        return this;
    }
    public DataSubMap remove(String key) {
        value.remove(key);
        return this;
    }
    public boolean hasKey(String key) {
        return value.hasKey(key);
    }
    @Override
    public byte[] serialize() {
        byte[] bytes = value.serializeMap();
        return ByteBuffer.allocate(4 + bytes.length).putInt(bytes.length).put(bytes).array();
    }
    @Override
    public DataSubMap loadFromBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buffer.get();
        }
        try {
            value = DataUtils.createMapFromBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public DataElement clone() {
        return new DataSubMap(value.clone());
    }

}
