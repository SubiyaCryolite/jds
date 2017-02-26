CREATE TABLE JdsStoreInteger(
    FieldId     BIGINT,
    EntityGuid    VARCHAR(48),
    Value       INTEGER,
    PRIMARY KEY (FieldId,EntityGuid)
);