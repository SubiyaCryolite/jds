CREATE TABLE JdsStoreEntityBinding
(
    ParentEntityGuid    VARCHAR(48),
    ChildEntityGuid     VARCHAR(48),
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);