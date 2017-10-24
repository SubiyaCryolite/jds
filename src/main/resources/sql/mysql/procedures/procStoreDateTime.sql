CREATE PROCEDURE procStoreDateTime(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue DATETIME)
BEGIN
	INSERT INTO JdsStoreDateTime(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END