CREATE TABLE JdsStoreLongArray(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           BIGINT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    CONSTRAINT fk_JdsStoreLongArray_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);