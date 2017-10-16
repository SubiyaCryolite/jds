CREATE PROCEDURE procStoreFloat(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue FLOAT)
BEGIN
	INSERT INTO JdsStoreFloat(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END