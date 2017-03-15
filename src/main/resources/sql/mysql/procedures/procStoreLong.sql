CREATE PROCEDURE procStoreLong(IN pEntityGuid VARCHAR(48), IN pFieldId BIGINT, IN pValue BIGINT)
BEGIN
	INSERT INTO JdsStoreLong(EntityGuid, FieldId, Value)
    VALUES (pEntityGuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END