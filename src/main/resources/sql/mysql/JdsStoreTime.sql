CREATE TABLE JdsStoreTime(
    FieldId     BIGINT,
    Uuid  VARCHAR(48),
    Value       TIME,
    PRIMARY KEY (FieldId,Uuid),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);