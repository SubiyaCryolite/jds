CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    EntityGuid  TEXT,
    Sequence    INTEGER,
    Value       BIGINT,
    PRIMARY KEY(FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);