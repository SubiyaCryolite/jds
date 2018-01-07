CREATE FUNCTION procStoreZonedDateTime(pUuid VARCHAR(96), pFieldId BIGINT, pValue TIMESTAMP WITH TIME ZONE)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreZonedDateTime(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;