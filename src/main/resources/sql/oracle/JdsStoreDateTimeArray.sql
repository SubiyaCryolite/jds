CREATE TABLE JdsStoreDateTimeArray(
    FieldId     NUMBER(19),
    Uuid  NVARCHAR2(96),
    Sequence    NUMBER(10),
    Value       TIMESTAMP,
    PRIMARY KEY(FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)