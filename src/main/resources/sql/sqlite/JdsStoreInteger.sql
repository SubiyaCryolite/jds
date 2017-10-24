CREATE TABLE JdsStoreInteger(
    FieldId         BIGINT,
    Uuid      TEXT,
    Value           INTEGER,
    PRIMARY KEY (FieldId,Uuid),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);