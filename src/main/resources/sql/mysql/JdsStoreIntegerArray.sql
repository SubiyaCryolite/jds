CREATE TABLE JdsStoreIntegerArray(
    FieldId     BIGINT,
    Uuid  VARCHAR(96),
    Sequence    INT,
    Value       INT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);