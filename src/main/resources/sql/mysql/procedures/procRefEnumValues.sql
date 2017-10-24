CREATE PROCEDURE procRefEnumValues(IN pFieldId BIGINT, IN pEnumSeq INT, IN pEnumValue TEXT)
BEGIN
	INSERT INTO JdsEnums(FieldId, EnumSeq, EnumValue)
    VALUES (pFieldId, pEnumSeq, pEnumValue)
    ON DUPLICATE KEY UPDATE EnumValue = pEnumValue;
END