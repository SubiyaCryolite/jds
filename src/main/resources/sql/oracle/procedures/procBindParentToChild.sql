CREATE PROCEDURE procBindParentToChild(pParentEntityCode IN NUMBER, pChildEntityCode IN NUMBER)
AS
BEGIN
    MERGE INTO JdsRefEntityInheritance dest
    USING DUAL ON (pParentEntityCode = ParentEntityCode AND pChildEntityCode = ChildEntityCode)
    WHEN NOT MATCHED THEN
        INSERT(ParentEntityCode, ChildEntityCode) VALUES(pParentEntityCode, pChildEntityCode);
END procBindParentToChild;