CREATE TABLE JdsStoreEntitySubclass(
    EntityGuid        NVARCHAR(48),
    SubEntityGuid     NVARCHAR(48),
    EntityId        BIGINT,
    PRIMARY KEY     (EntityGuid,SubEntityGuid,EntityId)
);