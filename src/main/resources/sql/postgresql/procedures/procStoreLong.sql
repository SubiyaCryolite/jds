CREATE FUNCTION procStoreLong(pUuid VARCHAR(48), pFieldId BIGINT, pValue BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreLong(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON CONFLICT (Uuid,FieldId) DO UPDATE SET Value = pValue;
END;
$$ LANGUAGE plpgsql;