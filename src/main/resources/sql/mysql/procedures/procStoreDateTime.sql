CREATE PROCEDURE procStoreDateTime(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue DATETIME)
BEGIN
	INSERT INTO JdsStoreDateTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END