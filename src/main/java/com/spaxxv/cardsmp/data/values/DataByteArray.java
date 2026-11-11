package com.spaxxv.cardsmp.data.values;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataByteArray extends DataElement {
    public byte[] value;
    public DataByteArray() {
        this(new byte[0]);
    }
    public DataByteArray(byte[] bytes) {
        value = bytes;
    }
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + value.length).putInt(value.length);
        buffer.put(value);
        return buffer.array();
    }
    @Override
    public DataByteArray loadFromBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buffer.get();
        }
        value = bytes;
        return this;
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }

    @Override
    public DataElement clone() {
        return new DataByteArray(value);
    }
}
