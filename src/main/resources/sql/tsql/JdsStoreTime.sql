CREATE TABLE JdsStoreTime(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48),
    Value           INTEGER,
    PRIMARY KEY (FieldId,EntityGuid),
    CONSTRAINT fk_JdsStoreTime_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);