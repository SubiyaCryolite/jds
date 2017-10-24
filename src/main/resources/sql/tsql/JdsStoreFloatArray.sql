CREATE TABLE JdsStoreFloatArray(
    FieldId         BIGINT,
    Uuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           REAL,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    CONSTRAINT fk_JdsStoreFloatArray_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);