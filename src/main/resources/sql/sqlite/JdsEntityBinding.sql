CREATE TABLE JdsEntityBinding(
    ParentUuid    TEXT,
    ChildUuid     TEXT,
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);