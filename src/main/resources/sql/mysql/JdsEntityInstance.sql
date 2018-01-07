CREATE TABLE JdsEntityInstance
(
    Uuid       VARCHAR(96),
    EntityId         BIGINT,
    CONSTRAINT unique_entity_inheritance UNIQUE (Uuid,EntityId)
);