CREATE TABLE JdsStoreDateTimeArray(
    FieldId         BIGINT,
    EntityGuid      TEXT,
    Sequence        INTEGER,
    Value           TIMESTAMP,
    PRIMARY KEY(FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);