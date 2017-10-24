CREATE FUNCTION procStoreTime(pUuid VARCHAR(48), pFieldId BIGINT, pValue TIME WITHOUT TIME ZONE)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreTime(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;