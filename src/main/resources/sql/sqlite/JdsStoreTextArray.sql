CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    Uuid    TEXT,
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);