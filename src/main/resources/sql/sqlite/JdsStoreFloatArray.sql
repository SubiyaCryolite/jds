CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    EntityGuid  TEXT,
    Sequence    INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);