CREATE TABLE JdsStoreBoolean(
    FieldId         BIGINT,
    EntityGuid      TEXT,
    Value           BOOLEAN,
    PRIMARY KEY (FieldId,EntityGuid),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);