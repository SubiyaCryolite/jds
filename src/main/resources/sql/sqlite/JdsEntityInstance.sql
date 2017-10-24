CREATE TABLE JdsEntityInstance
(
    Uuid       TEXT,
    EntityId         BIGINT,
    CONSTRAINT unique_entity_inheritance UNIQUE (Uuid,EntityId)
);