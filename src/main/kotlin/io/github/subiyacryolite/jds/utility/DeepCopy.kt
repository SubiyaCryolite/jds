package io.github.subiyacryolite.jds.utility

import java.io.*


object DeepCopy {

    /**
     *
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T : Any> clone(source: T): T? {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(source)
                    ByteArrayInputStream(byteArrayOutputStream.toByteArray()).use { byteArrayInputStream ->
                        ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                            return objectInputStream.readObject() as T
                        }
                    }
                }
            }
        } catch (e: IOException) {
            return null
        } catch (e: ClassNotFoundException) {
            return null
        }
    }
}