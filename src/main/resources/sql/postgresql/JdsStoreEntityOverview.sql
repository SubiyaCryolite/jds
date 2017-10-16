CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          VARCHAR(48),
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    Version             BIGINT,
    Live                BOOLEAN,
    PRIMARY KEY         (EntityGuid)
);