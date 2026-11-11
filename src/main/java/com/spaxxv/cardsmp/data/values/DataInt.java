package com.spaxxv.cardsmp.data.values;


import java.nio.ByteBuffer;

public class DataInt extends DataElement {
    public int value;
    public DataInt() {
        this(0);
    }
    public DataInt(int i) {
        value = i;
    }
    @Override
    public byte[] serialize() {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
    @Override
    public DataInt loadFromBytes(ByteBuffer buffer) {
        value = buffer.getInt();
        return this;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public DataElement clone() {
        return new DataInt(value);
    }

}
