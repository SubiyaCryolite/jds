CREATE TABLE JdsStoreEntityBinding(
    ParentEntityGuid    NVARCHAR2(48),
    ChildEntityGuid     NVARCHAR2(48),
    FieldId             NUMBER(19),
    ChildEntityId       NUMBER(19),
    FOREIGN KEY(ParentEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)