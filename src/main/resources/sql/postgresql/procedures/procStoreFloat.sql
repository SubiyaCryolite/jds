CREATE FUNCTION procStoreFloat(pUuid VARCHAR(48), pFieldId BIGINT, pValue REAL)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreFloat(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;