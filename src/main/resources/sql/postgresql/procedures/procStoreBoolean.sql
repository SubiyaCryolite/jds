CREATE FUNCTION procStoreBoolean(pUuid VARCHAR(48), pFieldId BIGINT, pValue BOOLEAN)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreBoolean(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;