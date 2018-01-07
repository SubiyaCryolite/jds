CREATE TABLE JdsStoreDateTimeArray(
    FieldId         BIGINT,
    Uuid      NVARCHAR(96) NOT NULL,
    Sequence        INTEGER,
    Value           DATETIME,
    PRIMARY KEY(FieldId,Uuid,Sequence),
    CONSTRAINT fk_JdsStoreDateTimeArray_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);