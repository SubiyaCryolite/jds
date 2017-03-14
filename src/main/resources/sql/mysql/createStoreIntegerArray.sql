CREATE TABLE JdsStoreIntegerArray(
    FieldId     BIGINT,
    EntityGuid  VARCHAR(48),
    Sequence    INT,
    Value       INT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);