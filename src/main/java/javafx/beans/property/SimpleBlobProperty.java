package javafx.beans.property;

import java.io.*;

/**
 * Created by ifunga on 18/06/2017.
 */
public class SimpleBlobProperty implements Serializable {
    private byte[] bytes;

    public final byte[] get() {
        return bytes;
    }

    public final void set(final byte[] bytes) {
        this.bytes = bytes;
    }

    public final void set(final InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            int line = 0;
            while ((line = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, line);
            }
            bytes = os.toByteArray();
        }
        inputStream.close();
    }

    public final InputStream getResourceAsStream() {
        return new ByteArrayInputStream(bytes);
    }

    public final boolean isEmpty() {
        return bytes == null || bytes.length == 0;
    }
}