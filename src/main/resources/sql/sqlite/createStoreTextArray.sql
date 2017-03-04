CREATE TABLE JdsStoreTextArray(
    FieldId     BIGINT,
    EntityGuid    TEXT,
    Sequence   INTEGER,
    Value       TEXT,
    PRIMARY KEY (FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);