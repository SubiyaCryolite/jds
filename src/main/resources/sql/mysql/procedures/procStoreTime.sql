CREATE PROCEDURE procStoreTime(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue INT)
BEGIN
	INSERT INTO JdsStoreTime(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END