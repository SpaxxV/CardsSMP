package com.spaxxv.cardsmp.data.values;


import java.nio.ByteBuffer;

public class DataFloat extends DataElement {
    public float value;
    public DataFloat() {
        this(0.0F);
    }
    public DataFloat(float f) {
        value = f;
    }
    @Override
    public byte[] serialize() {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }
    @Override
    public DataFloat loadFromBytes(ByteBuffer buffer) {
        value = buffer.getFloat();
        return this;
    }

    @Override
    public String toString() {
        return value + "F";
    }

    @Override
    public DataElement clone() {
        return new DataFloat(value);
    }
}
