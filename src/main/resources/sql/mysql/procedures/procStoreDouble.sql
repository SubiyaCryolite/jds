CREATE PROCEDURE procStoreDouble(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue DOUBLE)
BEGIN
	INSERT INTO JdsStoreDouble(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END