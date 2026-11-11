package com.spaxxv.cardsmp.data.values;


import java.nio.ByteBuffer;

public class DataLong extends DataElement {
    public long value;
    public DataLong() {
        this(0L);
    }
    public DataLong(long l) {
        value = l;
    }
    @Override
    public byte[] serialize() {
        return ByteBuffer.allocate(8).putLong(value).array();
    }
    @Override
    public DataLong loadFromBytes(ByteBuffer buffer) {
        value = buffer.getLong();
        return this;
    }

    @Override
    public String toString() {
        return value + "L";
    }

    @Override
    public DataElement clone() {
        return new DataLong(value);
    }
}
