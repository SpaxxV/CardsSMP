package com.spaxxv.cardsmp.data;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.data.values.*;
import com.spaxxv.cardsmp.util.MapBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DataUtils {
    private static final Map<Class<? extends DataElement>, Byte> classToId = new HashMap<>();
    private static final Map<Byte, Class<? extends DataElement>> idToClass = new HashMap<>();
    private static byte nextID = 0;

    public static Class<? extends DataElement> getClass(byte id) {
        return idToClass.get(id);
    }

    public static byte getId(Class<? extends DataElement> type) {
        return classToId.get(type);
    }

    public static void registerElementType(Class<? extends DataElement> type) {
        CardSMP.instance.getLogger().info("Registered class " + type.getName() + " to byte ID " + nextID);
        idToClass.put(nextID, type);
        classToId.put(type, nextID);
        nextID++;
    }

    public static void registerPrimitiveTypes() {
        registerElementType(DataInt.class);
        registerElementType(DataFloat.class);
        registerElementType(DataLong.class);
        registerElementType(DataString.class);
        registerElementType(DataSubMap.class);
        registerElementType(DataBoolean.class);
        registerElementType(DataByteArray.class);
    }

    public static DataMap createMapFromBytes(byte[] bytes) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        DataMap map = new DataMap();
        while (buffer.hasRemaining()) {
            int keyLength = buffer.getInt();
            StringBuilder key = new StringBuilder();
            for (int i = 0; i < keyLength; i++) {
                key.append(buffer.getChar());
            }
            byte id = buffer.get();
            Class<? extends DataElement> type = getClass(id);
            DataElement element = type.getConstructor().newInstance().loadFromBytes(buffer);
            map.set(key.toString(), element);
        }
        return map;
    }

    public static void writeMapToFile(DataMap map, File file) throws IOException {
        byte[] bytes = map.serializeMap();
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(bytes);
        stream.flush();
        stream.close();
    }

    public static DataMap readMapFromFile(File file) throws Exception {
        FileInputStream stream = new FileInputStream(file);
        DataMap map = createMapFromBytes(stream.readAllBytes());
        stream.close();
        return map;
    }
}
