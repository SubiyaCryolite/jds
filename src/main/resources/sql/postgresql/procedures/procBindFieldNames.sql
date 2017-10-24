CREATE FUNCTION procBindFieldNames(pFieldId BIGINT, pFieldName TEXT, pFieldDescription TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsFields(FieldId, FieldName, FieldDescription)
    VALUES (pFieldId, pFieldName, pFieldDescription)
    ON CONFLICT (FieldId) DO UPDATE SET FieldName = pFieldName, FieldDescription = pFieldDescription;
END;
$$ LANGUAGE plpgsql;