CREATE PROCEDURE procStoreBlob(IN pUuid VARCHAR(96), IN pFieldId BIGINT, IN pValue BLOB)
BEGIN
	INSERT INTO JdsStoreBlob(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END