CREATE TABLE JdsStoreTime(
    FieldId     NUMBER(19),
    Uuid  NVARCHAR2(48),
    Value       NUMBER(10),
    PRIMARY KEY (FieldId,Uuid),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)