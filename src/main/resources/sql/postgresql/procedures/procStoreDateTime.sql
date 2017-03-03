CREATE FUNCTION procStoreDateTime(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue TIMESTAMP)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreDateTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;