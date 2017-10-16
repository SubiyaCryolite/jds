CREATE TABLE JdsStoreEntityBinding(
    ParentEntityGuid    TEXT,
    ChildEntityGuid     TEXT,
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);