CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    Uuid  TEXT,
    Sequence    INTEGER,
    Value       REAL,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);