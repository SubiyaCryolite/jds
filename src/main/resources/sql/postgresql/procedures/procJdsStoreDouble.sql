CREATE FUNCTION procJdsStoreDouble(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue FLOAT)
RETURNS INTEGER AS $$
BEGIN
	INSERT INTO JdsStoreDouble(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
    RETURN 1;
END;
$$ LANGUAGE plpgsql;