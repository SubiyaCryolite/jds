CREATE TABLE JdsRefEntityInheritance(
    ParentEntityCode    NUMBER(19),
    ChildEntityCode     NUMBER(19),
    PRIMARY KEY (ParentEntityCode,ChildEntityCode)
)