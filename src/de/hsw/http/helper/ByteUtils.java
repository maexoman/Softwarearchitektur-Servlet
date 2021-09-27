package de.hsw.http.helper;

import java.util.Iterator;
import java.util.List;

public class ByteUtils {

    /**
     * Hiermit kann aus einer Dynamischen Byte-Liste, ein Byte-Array erstellt werden.
     * @param buffer Der dynamische Byte-Buffer.
     * @return Das entsprechende Byte-Array.
     */
    public static byte[] byteArrayFromDynamicBuffer (List<Byte> buffer) {
        int bufferSize = buffer.size();
        Iterator<Byte> iterator = buffer.iterator();
        byte[] result = new byte[bufferSize];
        int index = 0;
        while (iterator.hasNext()) {
            result [index] = iterator.next();
            index += 1;
        }
        return result;
    }

    public static byte[] concatByteArrays (byte[]... arrays) {

        byte[] result;
        int byteSize = 0;
        int pos = 0;
        for (int index = 0; index < arrays.length; index += 1) {
            byteSize += arrays [index].length;
        }

        result = new byte [byteSize];

        for (int index = 0; index < arrays.length; index += 1) {
            byte[] oneArray = arrays [index];
            for (int arrayIndex = 0; arrayIndex < oneArray.length; arrayIndex += 1) {
                result [pos] = oneArray [arrayIndex];
                pos += 1;
            }
        }

        return result;
    }

}
