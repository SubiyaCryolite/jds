CREATE FUNCTION procStoreDateTime(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue TIMESTAMP)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreDateTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;