CREATE TABLE JdsEntityBinding(
    ParentUuid    TEXT,
    ChildUuid     TEXT,
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE NO ACTION --we use REPLACE INTO that is not an upsert :(,
    FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE NO ACTION --we use REPLACE INTO that is not an upsert :(
);