CREATE TABLE JdsEntityInstance
(
    Uuid       NVARCHAR(96),
    EntityId         BIGINT,
    CONSTRAINT unique_entity_inheritance UNIQUE (Uuid,EntityId) --deliberately left out reference in case of shady individuals
);