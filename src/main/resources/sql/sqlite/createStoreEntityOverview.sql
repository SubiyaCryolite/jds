CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          TEXT,
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    Version             BIGINT,
    Live                INTEGER,
    PRIMARY KEY         (EntityGuid)
);