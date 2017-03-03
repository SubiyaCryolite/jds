CREATE TABLE JdsStoreDateTimeArray(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48) NOT NULL,
    Sequence        INTEGER,
    Value           DATETIME,
    PRIMARY KEY(FieldId,EntityGuid,Sequence),
    CONSTRAINT fk_JdsStoreDateTimeArray_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);