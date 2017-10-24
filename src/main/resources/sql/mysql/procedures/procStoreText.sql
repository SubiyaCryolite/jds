CREATE PROCEDURE procStoreText(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue TEXT)
BEGIN
	INSERT INTO JdsStoreText(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END