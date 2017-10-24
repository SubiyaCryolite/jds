CREATE TABLE JdsEntityBinding
(
    ParentUuid    NVARCHAR(48),
    ChildUuid     NVARCHAR(48),
    FieldId       BIGINT,
    ChildEntityId BIGINT,
    CONSTRAINT fk_JdsEntityBinding_ParentUuid FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid),
    CONSTRAINT fk_JdsEntityBinding_ChildUuid FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid)
);