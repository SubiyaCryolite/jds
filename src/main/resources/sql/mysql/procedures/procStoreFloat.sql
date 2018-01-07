CREATE PROCEDURE procStoreFloat(IN pUuid VARCHAR(96), IN pFieldId BIGINT, IN pValue FLOAT)
BEGIN
	INSERT INTO JdsStoreFloat(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END