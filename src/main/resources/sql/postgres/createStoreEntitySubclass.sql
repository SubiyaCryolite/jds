CREATE TABLE JdsStoreEntitySubclass(
    ActionId        VARCHAR(48),
    SubActionId     VARCHAR(48),
    EntityId        BIGINT,
    PRIMARY KEY     (ActionId,SubActionId,EntityId)
);