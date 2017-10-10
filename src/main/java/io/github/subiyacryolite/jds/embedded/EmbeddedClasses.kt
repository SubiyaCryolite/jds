package io.github.subiyacryolite.jds.embedded

import java.time.LocalDateTime

class JdsPortableStoreBlob(val entityGuid: String, val fieldId: Long, val value: ByteArray)

class JdsPortableStoreDateTime(val entityGuid: String, val fieldId: Long, val value: LocalDateTime)
class JdsPortableStoreDouble(val entityGuid: String, val fieldId: Long, val value: Double)
class JdsPortableStoreFloat(val entityGuid: String, val fieldId: Long, val value: Float)
class JdsPortableStoreInteger(val entityGuid: String, val fieldId: Long, val value: Int)
class JdsPortableStoreLong(val entityGuid: String, val fieldId: Long, val value: Long)
class JdsPortableStoreText(val entityGuid: String, val fieldId: Long, val value: String)

class JdsPortableStoreDateTimeArray(val entityGuid: String, val fieldId: Long, val index: Int, val value: LocalDateTime)
class JdsPortableStoreDoubleArray(val entityGuid: String, val fieldId: Long, val index: Int, val value: Double)
class JdsPortableStoreFloatArray(val entityGuid: String, val fieldId: Long, val index: Int, val value: Float)
class JdsPortableStoreIntegerArray(val entityGuid: String, val fieldId: Long, val index: Int, val value: Int)
class JdsPortableStoreLongArray(val entityGuid: String, val fieldId: Long, val index: Int, val value: Long)
class JdsPortableStoreTextArray(val entityGuid: String, val fieldId: Long, val index: Int, val value: String)

class JdsPortableStoreTime(val entityGuid: String, val fieldId: Long, val value: ByteArray)
class JdsPortableStoreZonedDateText(val entityGuid: String, val fieldId: Long, val value: ByteArray)