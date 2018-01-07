CREATE TABLE JdsStoreDateTimeArray(
    FieldId     BIGINT,
    Uuid  VARCHAR(96),
    Sequence    INT,
    Value       DATETIME,
    PRIMARY KEY(FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);