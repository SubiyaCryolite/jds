CREATE TABLE JdsEntityBinding
(
    ParentUuid    NVARCHAR(96),
    ChildUuid     NVARCHAR(96),
    FieldId       BIGINT,
    ChildEntityId BIGINT,
    CONSTRAINT fk_JdsEntityBinding_ParentUuid FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid),
    CONSTRAINT fk_JdsEntityBinding_ChildUuid FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid)
);