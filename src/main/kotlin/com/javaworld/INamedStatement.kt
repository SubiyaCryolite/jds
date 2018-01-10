package com.javaworld

import java.io.InputStream
import java.sql.*

/**
 * Created by ifunga on 20/07/2017.
 */
interface INamedStatement : Statement {

    @Throws(SQLException::class)
    fun setObject(name: String, value: Any)

    @Throws(SQLException::class)
    fun setString(name: String, value: String)

    @Throws(SQLException::class)
    fun setInt(name: String, value: Int)

    @Throws(SQLException::class)
    fun setLong(name: String, value: Long)

    @Throws(SQLException::class)
    fun setBoolean(name: String, value: Boolean)

    @Throws(SQLException::class)
    fun setBytes(name: String, value: ByteArray)

    @Throws(SQLException::class)
    fun setBlob(name: String, value: InputStream)

    @Throws(SQLException::class)
    fun setFloat(name: String, value: Float)

    @Throws(SQLException::class)
    fun setNull(name: String, value: Int)

    @Throws(SQLException::class)
    fun setDouble(name: String, value: Double)

    @Throws(SQLException::class)
    fun setTimestamp(name: String, value: Timestamp)

    fun getStatement(): PreparedStatement

    @Throws(SQLException::class)
    fun execute(): Boolean

    @Throws(SQLException::class)
    fun executeQuery(): ResultSet

    @Throws(SQLException::class)
    fun executeUpdate(): Int

    @Throws(SQLException::class)
    fun addBatch()
}
