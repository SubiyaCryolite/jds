CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          TEXT,
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    Version             BIGINT,
    Live                BOOLEAN,
    PRIMARY KEY         (EntityGuid)
);