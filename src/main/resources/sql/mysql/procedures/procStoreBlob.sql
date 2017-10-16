CREATE PROCEDURE procStoreBlob(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue BLOB)
BEGIN
	INSERT INTO JdsStoreBlob(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END