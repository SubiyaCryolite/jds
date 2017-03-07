CREATE FUNCTION procStoreText(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreText(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;