CREATE TABLE JdsEntityBinding(
    ParentUuid    NVARCHAR2(96),
    ChildUuid     NVARCHAR2(96),
    FieldId             NUMBER(19),
    ChildEntityId       NUMBER(19),
    FOREIGN KEY(ParentUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE,
    FOREIGN KEY(ChildUuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)