CREATE TABLE JdsStoreTime(
    FieldId     NUMBER(19),
    EntityGuid  NVARCHAR2(48),
    Value       NUMBER(10),
    PRIMARY KEY (FieldId,EntityGuid),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)