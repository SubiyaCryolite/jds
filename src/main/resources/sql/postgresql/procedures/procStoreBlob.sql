CREATE FUNCTION procStoreBlob(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue BYTEA)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreBlob(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;