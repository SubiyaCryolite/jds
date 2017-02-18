CREATE TABLE JdsRefEntityOverview
(
    ActionId        TEXT,
    DateCreated     TIMESTAMP,
    DateModified    TIMESTAMP,
    EntityId        BIGINT,
    PRIMARY KEY     (EntityId,ActionId)
);