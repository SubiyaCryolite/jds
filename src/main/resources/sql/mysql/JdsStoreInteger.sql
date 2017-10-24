CREATE TABLE JdsStoreInteger(
    FieldId     BIGINT,
    Uuid  VARCHAR(48),
    Value       INT,
    PRIMARY KEY (FieldId,Uuid),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);