CREATE PROCEDURE procBindFieldNames(IN pFieldId BIGINT, IN pFieldName TEXT)
BEGIN
	INSERT INTO JdsRefFields(FieldId, FieldName)
    VALUES (pFieldId, pFieldName)
    ON DUPLICATE KEY UPDATE FieldName = pFieldName;
END