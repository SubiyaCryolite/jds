CREATE TABLE JdsStoreEntitySubclass(
    EntityGuid        VARCHAR(48),
    SubEntityGuid     VARCHAR(48),
    EntityId        BIGINT,
    PRIMARY KEY     (EntityGuid,SubEntityGuid,EntityId)
);