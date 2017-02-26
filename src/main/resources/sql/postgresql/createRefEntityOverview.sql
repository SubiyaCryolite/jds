CREATE TABLE JdsRefEntityOverview
(
    EntityGuid        VARCHAR(48),
    DateCreated     TIMESTAMP,
    DateModified    TIMESTAMP,
    EntityId        BIGINT,
    PRIMARY KEY     (EntityId,EntityGuid)
);