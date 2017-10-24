CREATE TABLE JdsStoreTextArray(
    FieldId     NUMBER(19),
    Uuid  NVARCHAR2(48),
    Sequence    NUMBER(10),
    Value       NCLOB,
    PRIMARY KEY (FieldId,Uuid,Sequence),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)