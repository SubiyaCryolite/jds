CREATE PROCEDURE procRefEnumValues(pFieldId IN NUMBER, pEnumSeq INTEGER, pEnumValue IN NCLOB)
AS
BEGIN
	MERGE INTO JdsEnums dest
	USING DUAL ON (pFieldId = FieldId AND pEnumSeq = EnumSeq)
	WHEN NOT MATCHED THEN
		INSERT(FieldId, EnumSeq, EnumValue) VALUES(pFieldId, pEnumSeq, pEnumValue)
	WHEN MATCHED THEN
		UPDATE SET EnumValue = pEnumValue;
END procRefEnumValues;