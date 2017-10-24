CREATE TABLE JdsEntityBinding
(
    ParentUuid    VARCHAR(48),
    ChildUuid     VARCHAR(48),
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);