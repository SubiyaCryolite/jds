CREATE PROCEDURE procStoreZonedDateTime(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue TIMESTAMP)
BEGIN
	INSERT INTO JdsStoreZonedDateTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END