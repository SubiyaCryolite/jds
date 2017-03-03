CREATE TABLE JdsStoreInteger(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48),
    Value           INTEGER,
    PRIMARY KEY (FieldId,EntityGuid),
    CONSTRAINT fk_JdsStoreInteger_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);