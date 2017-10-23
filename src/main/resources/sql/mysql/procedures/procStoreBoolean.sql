CREATE PROCEDURE procStoreBoolean(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue BOOLEAN)
BEGIN
	INSERT INTO JdsStoreBoolean(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END