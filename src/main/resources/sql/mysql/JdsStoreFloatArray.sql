CREATE TABLE JdsStoreFloatArray(
    FieldId     BIGINT,
    Uuid  VARCHAR(48),
    Sequence    INT,
    Value       FLOAT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);