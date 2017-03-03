CREATE FUNCTION procStoreText(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreText(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;