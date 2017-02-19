CREATE TABLE JdsRefEntityOverview
(
    EntityGuid        NVARCHAR(48),
    DateCreated     DATETIME,
    DateModified    DATETIME,
    EntityId        BIGINT,
    PRIMARY KEY     (EntityId,EntityGuid)
);