CREATE TABLE JdsStoreLongArray(
    FieldId         BIGINT,
    Uuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           BIGINT,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    CONSTRAINT fk_JdsStoreLongArray_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);