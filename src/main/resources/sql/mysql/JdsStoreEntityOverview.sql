CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          VARCHAR(48),
    DateCreated         DATETIME DEFAULT CURRENT_TIMESTAMP,
    DateModified        DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY         (EntityGuid)
);