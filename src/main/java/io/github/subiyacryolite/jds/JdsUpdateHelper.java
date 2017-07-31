package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsImplementation;

/**
 * Internal helper class to handle JDS updates
 * Created by indana on 7/11/2017.
 */
class JdsUpdateHelper {

    /**
     * Columns to drop when upgrading from version 1 to version 2
     *
     * @param jdsDb the {@link JdsDb JdsDb} instance
     */
    public static void v1Tov2DropColumnStoreEntityOverview(JdsDb jdsDb) {
        //SQLite does NOT make dropping columns easy
        if (jdsDb.columnExists("JdsStoreEntityOverview", "EntityId") >= 1) {
            switch (jdsDb.getImplementation()) {
                case POSTGRES:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreEntityOverview DROP COLUMN entityid CASCADE;");//postgres makes everything lower case
                case TSQL:
                case MYSQL:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreEntityOverview DROP COLUMN EntityId;");
                    break;
            }
        }
    }

    /**
     * Method to add the new BlobValue column to an upgraded schema
     *
     * @param jdsDb the {@link JdsDb JdsDb} instance
     */
    public static void v1Tov2AddColumnStoreOldFieldValues(JdsDb jdsDb) {
        if (jdsDb.columnExists("JdsStoreOldFieldValues", "BlobValue") == 0) {
            switch (jdsDb.getImplementation()) {
                case MYSQL:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreOldFieldValues ADD COLUMN BlobValue BLOB;");
                    break;
                case TSQL:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreOldFieldValues ADD BlobValue VARBINARY(MAX);");
                    break;
                case POSTGRES:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreOldFieldValues ADD COLUMN BlobValue BYTEA;");
                    break;
                case SQLITE:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreOldFieldValues ADD COLUMN BlobValue BLOB;");
                    break;
            }
        }
    }

    /**
     * Method to migrate data from version 1 to version 2 of {@link JdsDb JdsDb}
     *
     * @param jdsDb the {@link JdsDb JdsDb} instance
     */
    public static void v1ToV2MigrateData(JdsDb jdsDb) {
        if (jdsDb.getImplementation() == JdsImplementation.SQLITE)
            return;//SQLite doest drop the column, thus this could prove problematic
        if (jdsDb.columnExists("JdsStoreEntityOverview", "EntityId") >= 1) {
            jdsDb.executeSqlFromString("INSERT INTO JdsStoreEntityInheritance(EntityGuid, EntityId) SELECT EntityGuid, EntityId FROM JdsStoreEntityOverview");
        }
    }

    /**
     * Method to add new columns to facilitate cascade on delete options
     *
     * @param jdsDb the {@link JdsDb JdsDb} instance
     */
    public static void v1Tov2AddColumnStoreEntityBindings(JdsDb jdsDb) {
        if (jdsDb.columnExists("JdsStoreEntityBinding", "CascadeOnDelete") == 0) {
            switch (jdsDb.getImplementation()) {
                case MYSQL:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreEntityBinding ADD CascadeOnDelete INT;");
                    break;
                case TSQL:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreEntityBinding ADD CascadeOnDelete INTEGER;");
                    break;
                case POSTGRES:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreEntityBinding ADD COLUMN CascadeOnDelete INTEGER;");
                    break;
                case SQLITE:
                    jdsDb.executeSqlFromString("ALTER TABLE JdsStoreEntityBinding ADD COLUMN CascadeOnDelete INTEGER;");
                    break;
            }
        }
    }
}
