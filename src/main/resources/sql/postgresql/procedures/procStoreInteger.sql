CREATE FUNCTION procStoreInteger(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue INTEGER)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreInteger(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;