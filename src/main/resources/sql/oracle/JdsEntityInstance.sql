CREATE TABLE JdsEntityInstance(
    Uuid       NVARCHAR2(48),
    EntityId         NUMBER(19),
    CONSTRAINT unique_entity_inheritance UNIQUE (Uuid,EntityId)
)