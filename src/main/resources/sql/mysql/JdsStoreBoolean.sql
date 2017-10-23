CREATE TABLE JdsStoreBoolean(
    FieldId     BIGINT,
    EntityGuid  VARCHAR(48),
    Value       BOOLEAN,
    PRIMARY KEY (FieldId,EntityGuid),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);