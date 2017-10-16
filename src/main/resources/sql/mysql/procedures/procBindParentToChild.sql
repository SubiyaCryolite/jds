CREATE PROCEDURE procBindParentToChild(IN pParentEntityCode BIGINT, IN pChildEntityCode BIGINT)
BEGIN
	INSERT INTO JdsRefEntityInheritance(ParentEntityCode, ChildEntityCode)
    VALUES (pParentEntityCode, pChildEntityCode)
    ON DUPLICATE KEY UPDATE ParentEntityCode = pParentEntityCode, ChildEntityCode = pChildEntityCode;
END