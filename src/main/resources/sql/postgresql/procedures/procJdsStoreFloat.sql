CREATE FUNCTION procJdsStoreFloat(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue REAL)
RETURNS INTEGER AS $$
BEGIN
	INSERT INTO JdsStoreFloat(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
    RETURN 1;
END;
$$ LANGUAGE plpgsql;