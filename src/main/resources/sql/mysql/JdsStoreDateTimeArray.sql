CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    EntityGuid  VARCHAR(48),
    Sequence    INT,
    Value       DATETIME,
    PRIMARY KEY(FieldId,EntityGuid,Sequence),
    FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);