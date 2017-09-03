package com.javaworld

import java.io.InputStream
import java.sql.*
import java.util.*

/*
* @author adam_crume
*/
class NamedPreparedStatement
/**
 * Creates a NamedPreparedStatement.  Wraps a call to
 * c.[ prepareStatement][Connection.prepareStatement].
 *
 * @param connection the database connection
 * @param query      the parameterized query
 * @throws SQLException if the statement could not be created
 */
@Throws(SQLException::class)
constructor(connection: Connection, query: String) : INamedStatement {

    private val preparedStatement: PreparedStatement

    val indexMap: HashMap<String, Array<Int>> = HashMap()

    init {
        val parsedQuery = parse(query, indexMap)
        preparedStatement = connection.prepareStatement(parsedQuery)
    }


    /**
     * Parses a query with named parameters.  The parameter-index mappings are
     * put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This
     * method is non-private so JUnit code can
     * test it.
     *
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    private fun parse(query: String, paramMap: MutableMap<String, Array<Int>>): String {
        // I was originally using regular expressions, but they didn't work well for ignoring
        // parameter-like strings inside quotes.
        val length = query.length
        val parsedQuery = StringBuffer(length)
        var inSingleQuote = false
        var inDoubleQuote = false
        var index = 1

        var i = 0
        while (i < length) {
            var c = query[i]
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true
                } else if (c == '"') {
                    inDoubleQuote = true
                } else if (c == ':' && i + 1 < length &&
                        Character.isJavaIdentifierStart(query[i + 1])) {
                    var j = i + 2
                    while (j < length && Character.isJavaIdentifierPart(query[j])) {
                        j++
                    }
                    val name = query.substring(i + 1, j)
                    c = '?' // replace the parameter with a question mark
                    i += name.length // skip past the end if the parameter

                    var indexList: Array<Int>? = paramMap[name]
                    if (indexList == null) {
                        indexList = arrayOf<Int>(1)
                        paramMap.put(name, indexList)
                    }
                    indexList[0] = index
                    index++
                }
            }
            parsedQuery.append(c)
            i++
        }

        // replace the lists of Integer objects with arrays of ints
        val itr = paramMap.entries.iterator()
        while (itr.hasNext()) {
            val entry = itr.next()
            val list = entry.value
            entry.setValue(list)
        }
        return parsedQuery.toString()
    }

    private fun getIndexes(name: String): Array<Int> {
        return indexMap[name] ?: throw IllegalArgumentException("Parameter not found: " + name)
    }

    @Throws(SQLException::class)
    fun setObject(name: String, value: Any) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setObject(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setBytes(name: String, value: ByteArray) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setBytes(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setBlob(name: String, value: InputStream) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setBlob(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setNull(name: String, value: Int) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setNull(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setString(name: String, value: String) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setString(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setInt(name: String, value: Int) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setInt(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setLong(name: String, value: Long) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setLong(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setFloat(name: String, value: Float) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setFloat(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setDouble(name: String, value: Double) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setDouble(indexes[i], value)
        }
    }

    @Throws(SQLException::class)
    override fun setTimestamp(name: String, value: Timestamp) {
        val indexes = getIndexes(name)
        for (i in indexes.indices) {
            preparedStatement.setTimestamp(indexes[i], value)
        }
    }

    override fun getStatement(): PreparedStatement {
        return preparedStatement
    }

    @Throws(SQLException::class)
    override fun execute(): Boolean {
        return preparedStatement.execute()
    }

    @Throws(SQLException::class)
    override fun executeQuery(): ResultSet {
        return preparedStatement.executeQuery()
    }

    @Throws(SQLException::class)
    override fun executeUpdate(): Int {
        return preparedStatement.executeUpdate()
    }

    @Throws(SQLException::class)
    override fun executeQuery(sql: String): ResultSet {
        return preparedStatement.resultSet
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String): Int {
        return preparedStatement.executeUpdate(sql)
    }

    @Throws(SQLException::class)
    override fun close() {
        preparedStatement.close()
    }

    @Throws(SQLException::class)
    override fun getMaxFieldSize(): Int {
        return preparedStatement.maxFieldSize
    }

    @Throws(SQLException::class)
    override fun setMaxFieldSize(max: Int) {
        preparedStatement.maxFieldSize = max
    }

    @Throws(SQLException::class)
    override fun getMaxRows(): Int {
        return preparedStatement.maxRows
    }

    @Throws(SQLException::class)
    override fun setMaxRows(max: Int) {
        preparedStatement.maxRows = max
    }

    @Throws(SQLException::class)
    override fun setEscapeProcessing(enable: Boolean) {
        preparedStatement.setEscapeProcessing(enable)
    }

    @Throws(SQLException::class)
    override fun getQueryTimeout(): Int {
        return preparedStatement.queryTimeout
    }

    @Throws(SQLException::class)
    override fun setQueryTimeout(seconds: Int) {
        preparedStatement.queryTimeout = seconds
    }

    @Throws(SQLException::class)
    override fun cancel() {
        preparedStatement.cancel()
    }

    @Throws(SQLException::class)
    override fun getWarnings(): SQLWarning {
        return preparedStatement.warnings
    }

    @Throws(SQLException::class)
    override fun clearWarnings() {
        preparedStatement.clearWarnings()
    }

    @Throws(SQLException::class)
    override fun setCursorName(name: String) {
        preparedStatement.setCursorName(name)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String): Boolean {
        return preparedStatement.execute(sql)
    }

    @Throws(SQLException::class)
    override fun getResultSet(): ResultSet {
        return preparedStatement.resultSet
    }

    @Throws(SQLException::class)
    override fun getUpdateCount(): Int {
        return preparedStatement.updateCount
    }

    @Throws(SQLException::class)
    override fun getMoreResults(): Boolean {
        return preparedStatement.moreResults
    }

    @Throws(SQLException::class)
    override fun setFetchDirection(direction: Int) {
        preparedStatement.fetchDirection = direction
    }

    @Throws(SQLException::class)
    override fun getFetchDirection(): Int {
        return preparedStatement.fetchDirection
    }

    @Throws(SQLException::class)
    override fun setFetchSize(rows: Int) {
        preparedStatement.fetchSize = rows
    }

    @Throws(SQLException::class)
    override fun getFetchSize(): Int {
        return preparedStatement.fetchSize
    }

    @Throws(SQLException::class)
    override fun getResultSetConcurrency(): Int {
        return preparedStatement.resultSetConcurrency
    }

    @Throws(SQLException::class)
    override fun getResultSetType(): Int {
        return preparedStatement.resultSetType
    }

    @Throws(SQLException::class)
    override fun addBatch(sql: String) {
        preparedStatement.addBatch(sql)
    }

    @Throws(SQLException::class)
    override fun clearBatch() {
        preparedStatement.clearBatch()
    }

    @Throws(SQLException::class)
    override fun addBatch() {
        preparedStatement.addBatch()
    }

    @Throws(SQLException::class)
    override fun executeBatch(): IntArray {
        return preparedStatement.executeBatch()
    }

    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        return preparedStatement.connection
    }

    @Throws(SQLException::class)
    override fun getMoreResults(current: Int): Boolean {
        return preparedStatement.getMoreResults(current)
    }

    @Throws(SQLException::class)
    override fun getGeneratedKeys(): ResultSet {
        return preparedStatement.generatedKeys
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String, autoGeneratedKeys: Int): Int {
        return preparedStatement.executeUpdate(sql, autoGeneratedKeys)
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String, columnIndexes: IntArray): Int {
        return preparedStatement.executeUpdate(sql, columnIndexes)
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String, columnNames: Array<String>): Int {
        return preparedStatement.executeUpdate(sql, columnNames)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String, autoGeneratedKeys: Int): Boolean {
        return preparedStatement.execute(sql, autoGeneratedKeys)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String, columnIndexes: IntArray): Boolean {
        return preparedStatement.execute(sql, columnIndexes)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String, columnNames: Array<String>): Boolean {
        return preparedStatement.execute(sql, columnNames)
    }

    @Throws(SQLException::class)
    override fun getResultSetHoldability(): Int {
        return preparedStatement.resultSetHoldability
    }

    @Throws(SQLException::class)
    override fun isClosed(): Boolean {
        return preparedStatement.isClosed
    }

    @Throws(SQLException::class)
    override fun setPoolable(poolable: Boolean) {
        preparedStatement.isPoolable = poolable
    }

    @Throws(SQLException::class)
    override fun isPoolable(): Boolean {
        return preparedStatement.isPoolable
    }

    @Throws(SQLException::class)
    override fun closeOnCompletion() {
        preparedStatement.closeOnCompletion()
    }

    @Throws(SQLException::class)
    override fun isCloseOnCompletion(): Boolean {
        return preparedStatement.isCloseOnCompletion
    }

    @Throws(SQLException::class)
    override fun <T> unwrap(iface: Class<T>): T {
        return preparedStatement.unwrap(iface)
    }

    @Throws(SQLException::class)
    override fun isWrapperFor(iface: Class<*>): Boolean {
        return preparedStatement.isWrapperFor(iface)
    }
}