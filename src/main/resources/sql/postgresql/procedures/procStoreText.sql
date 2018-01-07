CREATE FUNCTION procStoreText(pUuid VARCHAR(96), pFieldId BIGINT, pValue TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreText(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;