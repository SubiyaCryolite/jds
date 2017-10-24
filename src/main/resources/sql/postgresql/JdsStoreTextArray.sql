CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    Uuid    VARCHAR(48),
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);