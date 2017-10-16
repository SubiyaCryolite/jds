CREATE PROCEDURE procBindFieldNames(IN pFieldId BIGINT, IN pFieldName TEXT IN pFieldDescription TEXT)
BEGIN
	INSERT INTO JdsRefFields(FieldId, FieldName, FieldDescription)
    VALUES (pFieldId, pFieldName, pFieldDescription)
    ON DUPLICATE KEY UPDATE FieldName = pFieldName, FieldDescription = pFieldDescription;
END