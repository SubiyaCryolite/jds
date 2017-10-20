package javafx.beans.property

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.Serializable


/**
 * Created by ifunga on 18/06/2017.
 * This class was designed to store binary values in a backing byte array. The byte array can also be read as an input stream
 */
class SimpleBlobProperty(value: ByteArray) : BlobProperty(value)