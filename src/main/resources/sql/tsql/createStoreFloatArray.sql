CREATE TABLE JdsStoreFloatArray(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           REAL,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    CONSTRAINT fk_JdsStoreFloatArray_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);