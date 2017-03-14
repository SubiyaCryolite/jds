CREATE FUNCTION procStoreLong(pEntityGuid VARCHAR(48), pFieldId BIGINT, pValue INTEGER)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreLong(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON CONFLICT (EntityGuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;