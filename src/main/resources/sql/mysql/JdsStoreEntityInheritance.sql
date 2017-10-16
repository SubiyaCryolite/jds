CREATE TABLE JdsStoreEntityInheritance
(
    EntityGuid       VARCHAR(48),
    EntityId         BIGINT,
    CONSTRAINT unique_entity_inheritance UNIQUE (EntityGuid,EntityId)
);