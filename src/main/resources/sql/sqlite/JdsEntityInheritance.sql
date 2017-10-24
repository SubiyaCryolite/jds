CREATE TABLE JdsEntityInheritance(
    ParentEntityCode    BIGINT,
    ChildEntityCode     BIGINT,
    PRIMARY KEY (ParentEntityCode,ChildEntityCode)
);