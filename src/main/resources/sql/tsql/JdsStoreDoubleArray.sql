CREATE TABLE JdsStoreDoubleArray(
    FieldId         BIGINT,
    Uuid      NVARCHAR(96) NOT NULL,
    Sequence        INTEGER,
    Value           FLOAT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    CONSTRAINT fk_JdsStoreDoubleArray_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);