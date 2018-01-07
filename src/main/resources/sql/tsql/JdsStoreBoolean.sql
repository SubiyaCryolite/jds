CREATE TABLE JdsStoreBoolean(
    FieldId         BIGINT,
    Uuid      NVARCHAR(96),
    Value           BIT,
    PRIMARY KEY (FieldId,Uuid),
    CONSTRAINT fk_JdsStoreBoolean_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);