CREATE TABLE JdsStoreEntitySubclass(
    ActionId        TEXT, --PARENT
    SubActionId     TEXT, --CHILD
    EntityId        BIGINT, --PARENT TYPE
    PRIMARY KEY     (ActionId,SubActionId,EntityId)
);