CREATE TABLE JdsStoreTextArray(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48),
    Sequence        INTEGER,
    Value           NVARCHAR(MAX),
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    CONSTRAINT fk_JdsStoreTextArray_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);