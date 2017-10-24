CREATE FUNCTION procStoreInteger(pUuid VARCHAR(48), pFieldId BIGINT, pValue INTEGER)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreInteger(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;