CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    EntityGuid  VARCHAR(48),
    Sequence    INT,
    Value       BIGINT,
    PRIMARY KEY(FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);