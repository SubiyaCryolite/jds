CREATE TABLE JdsStoreLongArray(
    FieldId     BIGINT,
    Uuid  VARCHAR(48),
    Sequence    INT,
    Value       BIGINT,
    PRIMARY KEY(FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);