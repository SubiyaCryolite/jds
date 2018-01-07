CREATE TABLE JdsStoreTime(
    FieldId         BIGINT,
    Uuid      NVARCHAR(96),
    Value           TIME(7),
    PRIMARY KEY (FieldId,Uuid),
    CONSTRAINT fk_JdsStoreTime_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);