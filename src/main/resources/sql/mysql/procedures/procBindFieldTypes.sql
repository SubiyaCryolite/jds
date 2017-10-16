CREATE PROCEDURE procBindFieldTypes(IN pTypeId BIGINT, IN pTypeName TEXT)
BEGIN
	INSERT INTO JdsRefFieldTypes(TypeId, TypeName)
    VALUES (pTypeId, pTypeName)
    ON DUPLICATE KEY UPDATE TypeName = pTypeName;
END