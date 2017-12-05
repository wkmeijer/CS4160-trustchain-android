package nl.tudelft.cs4160.trustchain_android.appToApp.connection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * ByteBuffer implementation of an {@link InputStream}.
 * <p/>
 * Created by jaap on 5/26/16.
 */
public class ByteBufferinputStream extends InputStream {
    private ByteBuffer buffer;

    public ByteBufferinputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public synchronized int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        return buffer.get();
    }

    @Override
    public int read(byte[] bytes, int byteOffset, int byteCount) throws IOException {
        byteCount = Math.min(byteCount, buffer.remaining());
        buffer.get(bytes, byteOffset, byteCount);
        return byteCount;
    }
}
