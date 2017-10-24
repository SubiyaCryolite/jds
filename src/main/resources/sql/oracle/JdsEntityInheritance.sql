CREATE TABLE JdsEntityInheritance(
    ParentEntityCode    NUMBER(19),
    ChildEntityCode     NUMBER(19),
    PRIMARY KEY (ParentEntityCode,ChildEntityCode)
)