CREATE FUNCTION procJdsStoreDateTime(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue TIMESTAMP)
RETURNS INTEGER AS $$
BEGIN
	INSERT INTO JdsStoreDateTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
    RETURN 1;
END;
$$ LANGUAGE plpgsql;