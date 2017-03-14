CREATE FUNCTION procStoreDouble(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue FLOAT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreDouble(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;