CREATE FUNCTION procStoreDouble(pUuid VARCHAR(96), pFieldId BIGINT, pValue FLOAT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreDouble(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;