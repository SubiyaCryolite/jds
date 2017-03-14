CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          VARCHAR(48),
    DateCreated         DATETIME DEFAULT CURRENT_TIMESTAMP,
    DateModified        DATETIME DEFAULT CURRENT_TIMESTAMP,
    EntityId            BIGINT,
    PRIMARY KEY         (EntityGuid)
);