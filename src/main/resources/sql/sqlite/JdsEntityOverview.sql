CREATE TABLE JdsEntityOverview
(
    Uuid          TEXT,
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    Version             BIGINT,
    Live                BOOLEAN,
    PRIMARY KEY         (Uuid)
);