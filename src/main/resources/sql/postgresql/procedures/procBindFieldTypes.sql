CREATE FUNCTION procBindFieldTypes(pTypeId BIGINT, pTypeName TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsRefFieldTypes(TypeId, TypeName)
    VALUES (pTypeId, pTypeName)
    ON CONFLICT (TypeId) DO UPDATE SET TypeName = pTypeName;
END;
$$ LANGUAGE plpgsql;