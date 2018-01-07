CREATE TABLE JdsEntityInstance(
    Uuid       NVARCHAR2(96),
    EntityId         NUMBER(19),
    CONSTRAINT unique_entity_inheritance UNIQUE (Uuid,EntityId)
)