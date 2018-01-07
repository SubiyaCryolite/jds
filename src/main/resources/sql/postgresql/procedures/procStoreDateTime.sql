CREATE FUNCTION procStoreDateTime(pUuid VARCHAR(96), pFieldId BIGINT, pValue TIMESTAMP)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreDateTime(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;