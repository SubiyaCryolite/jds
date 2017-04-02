package io.github.subiyacryolite.jds.enums;

/**
 * Created by ifunga on 12/02/2017.
 */
public enum JdsEnumTable {
    StoreTextArray("JdsStoreTextArray", "sta"),
    StoreFloatArray("JdsStoreFloatArray", "sfa"),
    StoreIntegerArray("JdsStoreIntegerArray", "sia"),
    StoreLongArray("JdsStoreLongArray", "sla"),
    StoreDoubleArray("JdsStoreDoubleArray", "sda"),
    StoreDateTimeArray("JdsStoreDateTimeArray", "sdta"),
    StoreText("JdsStoreText", "st"),
    StoreFloat("JdsStoreFloat", "sf"),
    StoreInteger("JdsStoreInteger", "si"),
    StoreLong("JdsStoreLong", "sl"),
    StoreDouble("JdsStoreDouble", "sd"),
    StoreDateTime("JdsStoreDateTime", "sdt"),
    StoreOldFieldValues("JdsStoreOldFieldValues", "sof"),
    StoreEntityOverview("JdsStoreEntityOverview", "eo"),
    StoreEntityBinding("JdsStoreEntityBinding", "eb"),
    RefEntities("JdsRefEntities", "et"),
    RefEnumValues("JdsRefEnumValues", "env"),
    RefFields("JdsRefFields", "fd"),
    RefFieldTypes("JdsRefFieldTypes", "ft"),
    BindEntityFields("JdsBindEntityFields", "bef"),
    BindEntityEnums("JdsBindEntityEnums", "bee"),
    SaveText("procStoreText", ""),
    SaveFloat("procStoreFloat", ""),
    SaveInteger("procStoreInteger", ""),
    SaveLong("procStoreLong", ""),
    SaveDouble("procStoreDouble", ""),
    SaveDateTime("procStoreDateTime", ""),
    SaveEntity("procStoreEntityOverview", ""),
    MapEntityFields("procBindEntityFields", ""),
    MapEntityEnums("procBindEntityEnums", ""),
    MapClassName("procRefEntities", ""),
    MapEnumValues("procRefEnumValues", ""),
    CascadeEntityBinding("triggerEntityBindingCascade", "");

    private String name, prefix;

    JdsEnumTable(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public String toString() {
        return getName();
    }
}
