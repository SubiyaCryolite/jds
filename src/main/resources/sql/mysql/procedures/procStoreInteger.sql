CREATE PROCEDURE procStoreInteger(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue INT)
BEGIN
	INSERT INTO JdsStoreInteger(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END