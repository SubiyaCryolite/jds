CREATE FUNCTION procStoreFloat(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue REAL)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreFloat(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;