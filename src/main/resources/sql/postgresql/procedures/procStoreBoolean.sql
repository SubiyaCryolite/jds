CREATE FUNCTION procStoreBoolean(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue BOOLEAN)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreBoolean(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;