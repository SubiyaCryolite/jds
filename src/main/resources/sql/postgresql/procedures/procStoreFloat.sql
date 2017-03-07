CREATE FUNCTION procStoreFloat(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue REAL)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreFloat(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;