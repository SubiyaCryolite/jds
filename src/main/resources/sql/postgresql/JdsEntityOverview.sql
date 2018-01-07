CREATE TABLE JdsEntityOverview
(
    Uuid          VARCHAR(96),
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    Version             BIGINT,
    Live                BOOLEAN,
    PRIMARY KEY         (Uuid)
);