CREATE TABLE JdsStoreEntityInheritance
(
    EntityGuid       TEXT,
    EntityId         BIGINT,
    CONSTRAINT unique_entity_inheritance UNIQUE (EntityGuid,EntityId)
);