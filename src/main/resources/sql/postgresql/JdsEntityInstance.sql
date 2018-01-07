CREATE TABLE JdsEntityInstance
(
    Uuid       VARCHAR(96),
    EntityId   BIGINT,
    CONSTRAINT unique_entity_instance UNIQUE (Uuid,EntityId)
);