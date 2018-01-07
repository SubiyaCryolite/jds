CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    Uuid  VARCHAR(96),
    Sequence    INT,
    Value       TEXT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);