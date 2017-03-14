CREATE FUNCTION procStoreInteger(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue INT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreInteger(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;