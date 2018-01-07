CREATE TABLE JdsEntityBinding
(
    ParentUuid    VARCHAR(96),
    ChildUuid     VARCHAR(96),
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);