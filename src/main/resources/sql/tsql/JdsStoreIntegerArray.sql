CREATE TABLE JdsStoreIntegerArray(
    FieldId         BIGINT,
    Uuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           INTEGER,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    CONSTRAINT fk_JdsStoreIntegerArray_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);