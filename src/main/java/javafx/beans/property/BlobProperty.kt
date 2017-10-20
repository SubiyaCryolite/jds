package javafx.beans.property

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.Serializable

/**
 * Created by ifunga on 18/06/2017.
 * This class was designed to store binary values in a backing byte array. The byte array can also be read as an input stream
 */
open class BlobProperty : Serializable {
    private var bytes: ByteArray? = null

    /**
     * Constructor
     *
     * @param value byte array input
     */
    constructor(value: ByteArray)  {
        set(value)
    }

    /**
     * Constructor
     *
     * @param value input stream source
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(value: InputStream){
        set(value)
    }

    /**
     * Acquire the blob as an array of bytes
     *
     * @return the blob as an array of bytes
     */
    fun get(): ByteArray? {
        return bytes
    }

    /**
     * Set the blob
     *
     * @param bytes the blob as an array of bytes
     */
    fun set(bytes: ByteArray) {
        this.bytes = bytes
    }

    /**
     * Set the blob
     *
     * @param inputStream the blob as an input stream
     * @throws IOException possible IO exception
     */
    @Throws(IOException::class)
    fun set(inputStream: InputStream) {
        bytes = inputStream.use { it.readBytes(1024) }
    }

    /**
     * Acquire the blob as an input stream
     *
     * @return the blob as an input stream
     */
    val resourceAsStream: InputStream
        get() = ByteArrayInputStream(bytes!!)

    /**
     * Determine if the blob is empty
     *
     * @return true if the blob is empty
     */
    val isEmpty: Boolean
        get() = bytes == null || bytes!!.size == 0
}