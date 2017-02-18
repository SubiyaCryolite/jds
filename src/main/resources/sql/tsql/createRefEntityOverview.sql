CREATE TABLE JdsRefEntityOverview
(
    ActionId        NVARCHAR(48),
    DateCreated     DATETIME,
    DateModified    DATETIME,
    EntityId        BIGINT,
    PRIMARY KEY     (EntityId,ActionId)
);