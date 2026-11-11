package com.spaxxv.cardsmp.data.values;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataString extends DataElement {
    public String value;
    public DataString() {
        this("");
    }
    public DataString(String s) {
        value = s;
    }
    @Override
    public byte[] serialize() {
        char[] chars = value.toCharArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + chars.length * 2).putInt(chars.length);
        for (char c : chars) buffer.putChar(c);
        return buffer.array();
    }
    @Override
    public DataString loadFromBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) {
            str.append(buffer.getChar());
        }
        value = str.toString();
        return this;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public DataElement clone() {
        return new DataString(value);
    }

}
