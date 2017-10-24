CREATE PROCEDURE procStoreDouble(IN pUuid VARCHAR(48), IN pFieldId BIGINT, IN pValue DOUBLE)
BEGIN
	INSERT INTO JdsStoreDouble(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END