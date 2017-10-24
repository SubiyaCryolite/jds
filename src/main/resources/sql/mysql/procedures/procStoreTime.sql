CREATE PROCEDURE procStoreTime(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue TIME)
BEGIN
	INSERT INTO JdsStoreTime(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END