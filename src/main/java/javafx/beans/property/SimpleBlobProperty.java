package javafx.beans.property;

import java.io.*;

/**
 * Created by ifunga on 18/06/2017.
 * This class was designed to store binary values in a backing byte array. The byte array can also be read as an input stream
 */
public class SimpleBlobProperty implements Serializable {
    private byte[] bytes;

    /**
     * Required no-args constructor
     */
    public SimpleBlobProperty()
    {}

    /**
     * Constructor
     * @param value byte array input
     */
    public SimpleBlobProperty(byte[] value)
    {
        this();
        set(value);
    }

    /**
     * Constructor
     * @param value input stream source
     * @throws IOException
     */
    public SimpleBlobProperty(InputStream value) throws IOException {
        this();
        set(value);
    }

    /**
     * Acquire the blob as an array of bytes
     * @return the blob as an array of bytes
     */
    public final byte[] get() {
        return bytes;
    }

    /**
     * Set the blob
     * @param bytes the blob as an array of bytes
     */
    public final void set(final byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Set the blob
     * @param inputStream the blob as an input stream
     * @throws IOException possible IO exception
     */
    public final void set(final InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            int line;
            while ((line = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, line);
            }
            bytes = os.toByteArray();
        }
        inputStream.close();
    }

    /**
     * Acquire the blob as an input stream
     * @return the blob as an input stream
     */
    public final InputStream getResourceAsStream() {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Determine if the blob is empty
     * @return true if the blob is empty
     */
    public final boolean isEmpty() {
        return bytes == null || bytes.length == 0;
    }
}