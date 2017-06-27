package hr.zavrsni.zavrsnitest2.utils;

import java.nio.ByteBuffer;

public class BufferUtils {

    public static byte[] byteBufferToArray(ByteBuffer buffer) {
        ByteBuffer readOnly = buffer.asReadOnlyBuffer();
        readOnly.flip();
        int limit = readOnly.limit();
        byte[] bytes = new byte[limit];
        readOnly.get(bytes, 0, limit);
        return bytes;
    }

    public static void cloneByteBuffer(ByteBuffer original, ByteBuffer clone) {
        final ByteBuffer readOnlyCopy = original.asReadOnlyBuffer();

        readOnlyCopy.flip();
        clone.put(readOnlyCopy);
        clone.position(original.position());
        clone.limit(original.limit());
        clone.order(original.order());
    }

    // "from" buffer should be flipped before calling this method
    public static void cloneByteBuffer2(ByteBuffer from, ByteBuffer to) {
        final ByteBuffer readOnlyCopy = from.asReadOnlyBuffer();
        to.put(readOnlyCopy);
    }

}
