CREATE PROCEDURE procStoreBoolean(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue BOOLEAN)
BEGIN
	INSERT INTO JdsStoreBoolean(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END