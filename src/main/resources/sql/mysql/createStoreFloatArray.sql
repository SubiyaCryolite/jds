CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    EntityGuid  VARCHAR(48),
    Sequence    INT,
    Value       FLOAT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);