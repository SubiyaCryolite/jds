CREATE TABLE JdsRefEntityOverview
(
    EntityGuid        TEXT,
    DateCreated     TIMESTAMP,
    DateModified    TIMESTAMP,
    EntityId        BIGINT,
    PRIMARY KEY     (EntityId,EntityGuid)
);