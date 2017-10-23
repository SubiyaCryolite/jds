CREATE TABLE JdsStoreBoolean(
    FieldId         BIGINT,
    EntityGuid      NVARCHAR(48),
    Value           BIT,
    PRIMARY KEY (FieldId,EntityGuid),
    CONSTRAINT fk_JdsStoreBoolean_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);