CREATE TABLE JdsStoreTime(
    FieldId     NUMBER(19),
    Uuid        NVARCHAR2(96),
    Value       NUMBER(19),
    PRIMARY KEY (FieldId,Uuid),
    FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)