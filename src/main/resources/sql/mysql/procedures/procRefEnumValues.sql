CREATE FUNCTION procRefEnumValues(pFieldId BIGINT, pEnumSeq INT, pEnumValue TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsRefEnumValues(FieldId, EnumSeq, EnumValue)
    VALUES (pFieldId, pEnumSeq, pEnumValue)
    ON CONFLICT (FieldId,EnumSeq) DO UPDATE SET EnumValue = pEnumValue;
END;
$$ LANGUAGE plpgsql;