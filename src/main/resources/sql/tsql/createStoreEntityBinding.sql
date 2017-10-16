CREATE TABLE JdsStoreEntityBinding
(
    ParentEntityGuid    NVARCHAR(48),
    ChildEntityGuid     NVARCHAR(48),
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    CONSTRAINT fk_JdsStoreEntityBinding_ParentEntityGuid FOREIGN KEY(ParentEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid),
    CONSTRAINT fk_JdsStoreEntityBinding_ChildEntityGuid FOREIGN KEY(ChildEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid)
);