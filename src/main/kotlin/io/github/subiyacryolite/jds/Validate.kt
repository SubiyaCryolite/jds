package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.FieldType

object Validate {

    fun validateMonthDay(field: Field) {
        if (field.type != FieldType.MonthDay) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateYearMonth(field: Field) {
        if (field.type != FieldType.YearMonth) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validatePeriod(field: Field) {
        if (field.type != FieldType.Period) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateDuration(field: Field) {
        if (field.type != FieldType.Duration) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateUuid(field: Field) {
        if (field.type != FieldType.Uuid) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateBlob(field: Field) {
        if (field.type != FieldType.Blob) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateTime(field: Field) {
        if (field.type != FieldType.Time) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateDate(field: Field) {
        if (field.type != FieldType.Date) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateDateTime(field: Field) {
        if (field.type != FieldType.DateTime) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateZonedDateTime(field: Field) {
        if (field.type != FieldType.ZonedDateTime) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateEnum(field: Field) {
        if (field.type != FieldType.Enum && field.type != FieldType.EnumString) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateBoolean(field: Field) {
        if (field.type != FieldType.Boolean) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateShort(field: Field) {
        if (field.type != FieldType.Short) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateInt(field: Field) {
        if (field.type != FieldType.Int) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateFloat(field: Field) {
        if (field.type != FieldType.Float) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateDouble(field: Field) {
        if (field.type != FieldType.Double) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateLong(field: Field) {
        if (field.type != FieldType.Long) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateString(field: Field) {
        if (field.type != FieldType.String) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateDateTimeCollection(field: Field) {
        if (field.type != FieldType.DateTimeCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateEnumCollection(field: Field) {
        if (field.type != FieldType.EnumCollection && field.type != FieldType.EnumStringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateIntCollection(field: Field) {
        if (field.type != FieldType.IntCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateUuidCollection(field: Field) {
        if (field.type != FieldType.UuidCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateFloatCollection(field: Field) {
        if (field.type != FieldType.FloatCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateDoubleCollection(field: Field) {
        if (field.type != FieldType.DoubleCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateLongCollection(field: Field) {
        if (field.type != FieldType.LongCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }

    fun validateStringCollection(field: Field) {
        if (field.type != FieldType.StringCollection) {
            throw RuntimeException("Incorrect type supplied for field [$field]")
        }
    }
}