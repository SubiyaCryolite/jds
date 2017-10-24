CREATE TABLE JdsEntityOverview
(
    Uuid          VARCHAR(48),
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    Version             BIGINT,
    Live                BOOLEAN,
    PRIMARY KEY         (Uuid)
);