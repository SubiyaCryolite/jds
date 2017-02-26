package org.jenesis.jds.enums;

/**
 * Created by ifunga on 12/02/2017.
 */
public enum JdsTable {
    StoreTextArray("JdsStoreTextArray"),
    StoreFloatArray("JdsStoreFloatArray"),
    StoreIntegerArray("JdsStoreIntegerArray"),
    StoreLongArray("JdsStoreLongArray"),
    StoreDoubleArray("JdsStoreDoubleArray"),
    StoreDateTimeArray("JdsStoreDateTimeArray"),
    StoreText("JdsStoreText"),
    StoreFloat("JdsStoreFloat"),
    StoreInteger("JdsStoreInteger"),
    StoreLong("JdsStoreLong"),
    StoreDouble("JdsStoreDouble"),
    StoreDateTime("JdsStoreDateTime"),
    StoreEntitySubclass("JdsStoreEntitySubclass"),
    StoreOldFieldValues("JdsStoreOldFieldValues"),
    RefEntities("JdsRefEntities"),
    RefEnumValues("JdsRefEnumValues"),
    RefFields("JdsRefFields"),
    RefFieldTypes("JdsRefFieldTypes"),
    RefEntityOverview("JdsRefEntityOverview"),
    BindEntityFields("JdsBindEntityFields"),
    BindEntityEnums("JdsBindEntityEnums"),
    SaveText("procJdsStoreText"),
    SaveFloat("procJdsStoreFloat"),
    SaveInteger("procJdsStoreInteger"),
    SaveLong("procJdsStoreLong"),
    SaveDouble("procJdsStoreDouble"),
    SaveDateTime("procJdsStoreDateTime");

    private String name;

    JdsTable(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
