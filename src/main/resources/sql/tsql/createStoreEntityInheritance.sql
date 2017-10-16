CREATE TABLE JdsStoreEntityInheritance
(
    EntityGuid       NVARCHAR(48),
    EntityId         BIGINT,
    CONSTRAINT unique_entity_inheritance UNIQUE (EntityGuid,EntityId) --deliberately left out reference in case of shady individuals
);