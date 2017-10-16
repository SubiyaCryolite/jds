CREATE TABLE JdsStoreDoubleArray(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           FLOAT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    CONSTRAINT fk_JdsStoreDoubleArray_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);