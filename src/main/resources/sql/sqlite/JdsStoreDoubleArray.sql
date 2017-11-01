CREATE TABLE JdsStoreDoubleArray(
    FieldId         BIGINT,
    Uuid      TEXT,
    Sequence        INTEGER,
    Value           DOUBLE,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE NO ACTION --we use REPLACE INTO that is not an upsert :(
);