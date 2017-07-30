CREATE TABLE JdsStoreTextArray(
    FieldId     NUMBER(19),
    EntityGuid  NVARCHAR2(48),
    Sequence    NUMBER(10),
    Value       NCLOB,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)