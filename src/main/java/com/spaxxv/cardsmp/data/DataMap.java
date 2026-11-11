package com.spaxxv.cardsmp.data;

import com.spaxxv.cardsmp.data.values.DataElement;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class DataMap {
    private Map<String, DataElement> map = new HashMap<>();
    public DataElement get(String key) {
        return map.get(key);
    }
    public DataMap set(String key, DataElement value) {
        map.put(key, value);
        return this;
    }
    public DataMap remove(String key) {
        map.remove(key);
        return this;
    }
    public boolean hasKey(String key) {
        return map.containsKey(key);
    }
    public byte[] serializeMap() {
        byte[] result = new byte[0];
        for (Map.Entry<String, DataElement> entry : map.entrySet()) {
            char[] key = entry.getKey().toCharArray();
            byte id = DataUtils.getId(entry.getValue().getClass());
            byte[] value = entry.getValue().serialize();
            byte[] merge = new byte[result.length + 4 + (key.length * 2) + 1 + value.length];
            ByteBuffer buffer = ByteBuffer.wrap(merge).put(result).putInt(key.length);
            for (char c : key) buffer.putChar(c);
            result = buffer.put(id).put(value).array();
        }
        return result;
    }
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        int c = 0;
        for (Map.Entry<String, DataElement> entry : map.entrySet()) {
            stringBuilder.append("\"").append(entry.getKey()).append("\":").append(entry.getValue().toString());
            c++;
            if (c < map.size()) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.append("}").toString();
    }

    @Override
    public DataMap clone() {
        DataMap newMap = new DataMap();
        for (Map.Entry<String, DataElement> entry : map.entrySet()) {
            newMap.set(entry.getKey(), entry.getValue().clone());
        }
        return newMap;
    }
}
