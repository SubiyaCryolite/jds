CREATE TABLE JdsStoreEntitySubclass(
    ActionId        NVARCHAR(48),
    SubActionId     NVARCHAR(48),
    EntityId        BIGINT,
    PRIMARY KEY     (ActionId,SubActionId,EntityId)
);