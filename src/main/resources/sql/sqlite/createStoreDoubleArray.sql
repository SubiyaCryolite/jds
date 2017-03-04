CREATE TABLE JdsStoreDoubleArray(
    FieldId         BIGINT,
    EntityGuid      TEXT,
    Sequence        INTEGER,
    Value           DOUBLE,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);