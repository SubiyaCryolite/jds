CREATE FUNCTION procBindParentToChild(pParentEntityCode BIGINT, pChildEntityCode BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsRefEntityInheritance(ParentEntityCode, ChildEntityCode)
    VALUES (pParentEntityCode, pChildEntityCode)
    ON CONFLICT (ParentEntityCode, ChildEntityCode) DO UPDATE SET ParentEntityCode = pParentEntityCode, ChildEntityCode = pChildEntityCode;
END;
$$ LANGUAGE plpgsql;