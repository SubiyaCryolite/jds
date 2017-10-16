CREATE FUNCTION procBindFieldNames(pFieldId BIGINT, pFieldName TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsRefFields(FieldId, FieldName)
    VALUES (pFieldId, pFieldName)
    ON CONFLICT (FieldId) DO UPDATE SET FieldName = pFieldName;
END;
$$ LANGUAGE plpgsql;