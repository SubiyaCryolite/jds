CREATE TABLE JdsEntityOverview
(
    Uuid          VARCHAR(96),
    DateCreated         DATETIME DEFAULT CURRENT_TIMESTAMP,
    DateModified        DATETIME DEFAULT CURRENT_TIMESTAMP,
    Version             BIGINT,
    Live                BOOLEAN,
    PRIMARY KEY         (Uuid)
);