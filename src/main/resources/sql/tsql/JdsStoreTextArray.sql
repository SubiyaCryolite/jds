CREATE TABLE JdsStoreTextArray(
    FieldId         BIGINT,
    Uuid      NVARCHAR(96),
    Sequence        INTEGER,
    Value           NVARCHAR(MAX),
    PRIMARY KEY (FieldId,Uuid,Sequence),
    CONSTRAINT fk_JdsStoreTextArray_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);