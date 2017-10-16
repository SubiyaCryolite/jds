CREATE PROCEDURE procStoreText(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue TEXT)
BEGIN
	INSERT INTO JdsStoreText(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END