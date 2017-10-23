CREATE TABLE JdsStoreEntityOverview(
    EntityGuid      NVARCHAR2(48),
    DateCreated     DATE,
    DateModified    DATE,
    Version         NUMBER(19),
    Live            NUMBER(3),
    PRIMARY KEY     (EntityGuid)
)