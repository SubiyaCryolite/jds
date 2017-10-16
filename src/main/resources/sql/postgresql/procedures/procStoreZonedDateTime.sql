CREATE FUNCTION procStoreZonedDateTime(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreZonedDateTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;