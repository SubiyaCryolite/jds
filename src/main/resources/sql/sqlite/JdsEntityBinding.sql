CREATE TABLE JdsEntityBinding(
    ParentUuid    TEXT,
    ChildUuid     TEXT,
    FieldId             BIGINT,
    ChildEntityId       BIGINT,
    FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid) DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity,
    FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid) DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);