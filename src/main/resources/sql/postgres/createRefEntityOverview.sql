CREATE TABLE JdsRefEntityOverview
(
    ActionId        VARCHAR(48),
    DateCreated     TIMESTAMP,
    DateModified    TIMESTAMP,
    EntityId        BIGINT,
    PRIMARY KEY     (EntityId,ActionId)
);