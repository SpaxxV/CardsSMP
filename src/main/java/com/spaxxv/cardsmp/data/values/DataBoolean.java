package com.spaxxv.cardsmp.data.values;


import com.spaxxv.cardsmp.CardSMP;

import java.nio.ByteBuffer;

public class DataBoolean extends DataElement {
    public boolean value;
    public DataBoolean() {
        this(false);
    }
    public DataBoolean(boolean b) {
        value = b;
    }
    @Override
    public byte[] serialize() {
        byte b = 0;
        if (value) b = 1;
        return ByteBuffer.allocate(1).put(b).array();
    }
    @Override
    public DataBoolean loadFromBytes(ByteBuffer buffer) {
        byte b = buffer.get();
        if (b != 0) value = true;
        return this;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public DataElement clone() {
        return new DataBoolean(value);
    }
}
