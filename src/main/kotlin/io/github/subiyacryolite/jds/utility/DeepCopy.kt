package io.github.subiyacryolite.jds.utility

import java.io.*


object DeepCopy {

    /**
     *
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> clone(source: T): T {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                objectOutputStream.writeObject(source)
            }
            ByteArrayInputStream(byteArrayOutputStream.toByteArray()).use { byteArrayInputStream ->
                ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                    @Suppress("unchecked_cast")
                    return objectInputStream.readObject() as T
                }
            }
        }
    }
}