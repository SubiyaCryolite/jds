CREATE PROCEDURE procStoreZonedDateTime(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue TIMESTAMP)
BEGIN
	INSERT INTO JdsStoreZonedDateTime(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END