CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    Uuid    VARCHAR(96),
    Sequence   INTEGER,
    Value       TIMESTAMP,
    PRIMARY KEY(FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);