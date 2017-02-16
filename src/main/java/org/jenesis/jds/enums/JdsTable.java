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
    StoreEntities("JdsStoreEntities"),
    StoreEntitySubclass("JdsStoreEntitySubclass"),
    RefEnumValues("JdsRefEnumValues"),
    RefFields("JdsRefFields"),
    RefFieldTypes("JdsRefFieldTypes"),
    BindEntityFields("JdsBindEntityFields"),
    BindEntityEnums("JdsBindEntityEnums"),
    RefEntityOverview("JdsRefEntityOverview"),
    RefOldFieldValues("JdsRefOldFieldValues");

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
