CREATE FUNCTION procJdsStoreText(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue TEXT)
RETURNS INTEGER AS $$
BEGIN
	INSERT INTO JdsStoreText(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON conflict (EntityGuid,FieldId) do
    UPDATE SET Value = pValue;
    RETURN 1;
END;
$$ LANGUAGE plpgsql;