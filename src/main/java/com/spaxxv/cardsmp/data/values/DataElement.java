package com.spaxxv.cardsmp.data.values;

import java.nio.ByteBuffer;

public abstract class DataElement {
    public abstract byte[] serialize();
    public abstract DataElement loadFromBytes(ByteBuffer buffer);
    public abstract String toString();
    public abstract DataElement clone();
}
