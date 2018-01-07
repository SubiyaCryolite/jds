CREATE TABLE JdsStoreBoolean(
    FieldId     BIGINT,
    Uuid  VARCHAR(96),
    Value       BOOLEAN,
    PRIMARY KEY (FieldId,Uuid),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);