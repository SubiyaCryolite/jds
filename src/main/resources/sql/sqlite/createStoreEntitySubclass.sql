CREATE TABLE JdsStoreEntitySubclass(
    EntityGuid        TEXT, --PARENT
    SubEntityGuid     TEXT, --CHILD
    EntityId        BIGINT, --PARENT TYPE
    PRIMARY KEY     (EntityGuid,SubEntityGuid,EntityId)
);