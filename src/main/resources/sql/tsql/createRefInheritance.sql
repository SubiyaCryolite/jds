CREATE TABLE JdsRefEntityInheritance(
    ParentEntityCode    BIGINT,
    ChildEntityCode     BIGINT,
    PRIMARY KEY (ParentEntityCode,ChildEntityCode)
);