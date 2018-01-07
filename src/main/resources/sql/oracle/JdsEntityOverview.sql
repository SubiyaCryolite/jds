CREATE TABLE JdsEntityOverview(
    Uuid      NVARCHAR2(96),
    DateCreated     TIMESTAMP,
    DateModified    TIMESTAMP,
    Version         NUMBER(19),
    Live            NUMBER(3),
    PRIMARY KEY     (Uuid)
)