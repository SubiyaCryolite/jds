package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.enums.FieldType
import java.sql.Connection

object EntityUtils {

    /**
     * Binds all the fieldIds attached to an entity, updates the fieldIds dictionary
     * @param dbContext
     * @param connection
     * @param entityId
     */
    internal fun populateRefFieldRefEntityField(
            dbContext: DbContext,
            connection: Connection,
            entityId: Int
    ) = try {

        val clearFieldTag = connection.prepareStatement("DELETE FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.FieldTag)} WHERE field_id = ?")
        val clearFieldAlternateCode = connection.prepareStatement("DELETE FROM ${dbContext.getName(io.github.subiyacryolite.jds.enums.Table.FieldAlternateCode)} WHERE field_id = ?")
        val populateField = dbContext.getCallOrStatement(connection, dbContext.populateField())
        val populateEntityField = dbContext.getCallOrStatement(connection, dbContext.populateEntityField())
        val populateFieldEntity = dbContext.getCallOrStatement(connection, dbContext.populateFieldEntity())
        val populateFieldTag = dbContext.getCallOrStatement(connection, dbContext.populateFieldTag())
        val populateFieldAlternateCode = dbContext.getCallOrStatement(connection, dbContext.populateFieldAlternateCode())

        Entity.getFields(entityId).forEach { fieldId ->
            val field = Field.values.getValue(fieldId)

            clearFieldTag.setInt(1, field.id)
            clearFieldTag.addBatch()

            clearFieldAlternateCode.setInt(1, field.id)
            clearFieldAlternateCode.addBatch()

            populateField.setInt(1, field.id)
            populateField.setString(2, field.name)
            populateField.setString(3, field.description)
            populateField.setInt(4, field.type.ordinal)
            populateField.addBatch()

            populateEntityField.setInt(1, entityId)
            populateEntityField.setInt(2, field.id)
            populateEntityField.addBatch()

            field.tags.forEach { tag ->
                populateFieldTag.setInt(1, field.id)
                populateFieldTag.setString(2, tag)
                populateFieldTag.addBatch()
            }

            field.alternateCodes.forEach { (alternateCode, value) ->
                populateFieldAlternateCode.setInt(1, field.id)
                populateFieldAlternateCode.setString(2, alternateCode)
                populateFieldAlternateCode.setString(3, value)
                populateFieldAlternateCode.addBatch()
            }

            if (field.type == FieldType.Entity || field.type == FieldType.EntityCollection) {
                val fieldEntity = FieldEntity.values[field.id]
                if (fieldEntity != null) {
                    val entityAnnotation = Entity.getEntityAnnotation(fieldEntity.entity)
                    if (entityAnnotation != null) {
                        populateFieldEntity.setInt(1, field.id)
                        populateFieldEntity.setInt(2, entityAnnotation.id)
                        populateFieldEntity.addBatch()
                    }
                }
            }
        }

        clearFieldTag.use { statement ->
            statement.executeBatch()
        }
        clearFieldAlternateCode.use { statement ->
            statement.executeBatch()
        }
        populateField.use { statement ->
            statement.executeBatch()
        }
        populateEntityField.use { statement ->
            statement.executeBatch()
        }
        populateFieldTag.use { statement ->
            statement.executeBatch()
        }
        populateFieldAlternateCode.use { statement ->
            statement.executeBatch()
        }
        populateFieldEntity.use { statement ->
            statement.executeBatch()
        }

    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Binds all the enumValues attached to an entity
     * @param connection
     * @param entityId
     * @param dbContext
     */
    @Synchronized
    internal fun populateRefEnumRefEntityEnum(
            dbContext: DbContext,
            connection: Connection,
            entityId: Int
    ) {
        val enums = Entity.getEnums(entityId)
        populateRefEnum(dbContext, connection, enums)
        populateRefEntityEnum(dbContext, connection, entityId, enums)
        if (dbContext.options.logOutput) {
            System.out.printf("Mapped Enums for Entity[%s]\n", entityId)
        }
    }

    /**
     * Binds all the enumValues attached to an entity
     * @param dbContext
     * @param connection the SQL connection to use for DB operations
     * @param entityId the value representing the entity
     * @param fieldIds the entity's enumValues
     */
    @Synchronized
    private fun populateRefEntityEnum(
            dbContext: DbContext,
            connection: Connection,
            entityId: Int,
            fieldIds: Collection<Int>
    ) = try {
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateEntityEnum()) else connection.prepareStatement(dbContext.populateEntityEnum())).use { statement ->
            for (fieldId in fieldIds) {
                val jdsFieldEnum = FieldEnum.enums[fieldId]!!
                statement.setInt(1, entityId)
                statement.setInt(2, jdsFieldEnum.field.id)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }

    /**
     * Binds all the values attached to an enum
     * @param dbContext
     * @param connection the SQL connection to use for DB operations
     * @param fieldIds the jdsField enum
     */
    @Synchronized
    private fun populateRefEnum(
            dbContext: DbContext,
            connection: Connection,
            fieldIds: Collection<Int>
    ) = try {
        (if (dbContext.supportsStatements) connection.prepareCall(dbContext.populateEnum()) else connection.prepareStatement(dbContext.populateEnum())).use { statement ->
            for (fieldId in fieldIds) {
                val jdsFieldEnum = FieldEnum.enums[fieldId]!!
                jdsFieldEnum.values.forEach { enum ->
                    statement.setInt(1, jdsFieldEnum.field.id)
                    statement.setInt(2, enum.ordinal)
                    statement.setString(3, enum.name)
                    statement.setString(4, enum.toString())
                    statement.addBatch()
                }
            }
            statement.executeBatch()
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
    }
}