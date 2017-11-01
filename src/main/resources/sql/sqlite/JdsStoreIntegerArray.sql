CREATE TABLE JdsStoreIntegerArray(
    FieldId         BIGINT,
    Uuid      TEXT,
    Sequence        INTEGER,
    Value           INTEGER,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE NO ACTION --we use REPLACE INTO that is not an upsert :(
);