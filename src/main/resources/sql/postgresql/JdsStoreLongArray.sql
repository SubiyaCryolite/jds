CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    Uuid    VARCHAR(96),
    Sequence   INTEGER,
    Value       BIGINT,
    PRIMARY KEY(FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);