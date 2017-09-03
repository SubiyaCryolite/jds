package io.github.subiyacryolite.jds

import io.github.subiyacryolite.jds.enums.JdsImplementation

import java.sql.Connection

/**
 * Internal helper class to handle JDS updates
 * Created by indana on 7/11/2017.
 */
object JdsUpdateHelper {

    /**
     * Columns to drop when upgrading from version 1 to version 2
     *
     * @param jdsDb the [JdsDb] instance
     */
    fun v1Tov2DropColumnStoreEntityOverview(connection: Connection, jdsDb: JdsDb) {
        //SQLite does NOT make dropping columns easy
        if (jdsDb.columnExists(connection, "JdsStoreEntityOverview", "EntityId") >= 1) {
            when (jdsDb.implementation) {
                JdsImplementation.POSTGRES -> {
                    jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityOverview DROP COLUMN entityid CASCADE;")//postgres makes everything lower case
                    jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityOverview DROP COLUMN EntityId;")
                }
                JdsImplementation.TSQL, JdsImplementation.MYSQL -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityOverview DROP COLUMN EntityId;")
            }
        }
    }

    /**
     * Method to add the new BlobValue column to an upgraded schema
     *
     * @param jdsDb the [JdsDb] instance
     */
    fun v1Tov2AddColumnStoreOldFieldValues(connection: Connection, jdsDb: JdsDb) {
        if (jdsDb.columnExists(connection, "JdsStoreOldFieldValues", "BlobValue") == 0) {
            when (jdsDb.implementation) {
                JdsImplementation.MYSQL -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreOldFieldValues ADD COLUMN BlobValue BLOB;")
                JdsImplementation.TSQL -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreOldFieldValues ADD BlobValue VARBINARY(MAX);")
                JdsImplementation.POSTGRES -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreOldFieldValues ADD COLUMN BlobValue BYTEA;")
                JdsImplementation.SQLITE -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreOldFieldValues ADD COLUMN BlobValue BLOB;")
            }
        }
    }

    /**
     * Method to migrate data from version 1 to version 2 of [JdsDb]
     *
     * @param jdsDb the [JdsDb] instance
     */
    fun v1ToV2MigrateData(connection: Connection, jdsDb: JdsDb) {
        if (jdsDb.implementation === JdsImplementation.SQLITE)
            return //SQLite doest drop the column, thus this could prove problematic
        if (jdsDb.columnExists(connection, "JdsStoreEntityOverview", "EntityId") >= 1) {
            jdsDb.executeSqlFromString(connection, "INSERT INTO JdsStoreEntityInheritance(EntityGuid, EntityId) SELECT EntityGuid, EntityId FROM JdsStoreEntityOverview")
        }
    }

    /**
     * Method to add new columns to facilitate cascade on delete options
     *
     * @param jdsDb the [JdsDb] instance
     */
    fun v1Tov2AddColumnStoreEntityBindings(connection: Connection, jdsDb: JdsDb) {
        if (jdsDb.columnExists(connection, "JdsStoreEntityBinding", "CascadeOnDelete") == 0) {
            when (jdsDb.implementation) {
                JdsImplementation.MYSQL -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityBinding ADD CascadeOnDelete INT;")
                JdsImplementation.TSQL -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityBinding ADD CascadeOnDelete INTEGER;")
                JdsImplementation.POSTGRES -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityBinding ADD COLUMN CascadeOnDelete INTEGER;")
                JdsImplementation.SQLITE -> jdsDb.executeSqlFromString(connection, "ALTER TABLE JdsStoreEntityBinding ADD COLUMN CascadeOnDelete INTEGER;")
            }
        }
    }
}
