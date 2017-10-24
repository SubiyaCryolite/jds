CREATE FUNCTION procStoreBlob(pUuid VARCHAR(48), pFieldId BIGINT, pValue BYTEA)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreBlob(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;