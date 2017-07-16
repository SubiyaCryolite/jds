package io.github.subiyacryolite.jds;

import io.github.subiyacryolite.jds.enums.JdsImplementation;

/**
 * Internal helper class to handle update
 * Created by indana on 7/11/2017.
 */
class JdsUpdateHelper {

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

    public static void v1ToV2MigrateData(JdsDb jdsDb) {
        if (jdsDb.getImplementation() == JdsImplementation.SQLITE)
            return;//SQLite doest drop the column, thus this could prove problematic
        if (jdsDb.columnExists("JdsStoreEntityOverview", "EntityId") >= 1) {
            jdsDb.executeSqlFromString("INSERT INTO JdsStoreEntityInheritance(EntityGuid, EntityId) SELECT EntityGuid, EntityId FROM JdsStoreEntityOverview");
        }
    }

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
