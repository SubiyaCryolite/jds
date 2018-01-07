CREATE PROCEDURE procStoreLong(IN pUuid VARCHAR(96), IN pFieldId BIGINT, IN pValue BIGINT)
BEGIN
	INSERT INTO JdsStoreLong(Uuid, FieldId, Value)
    VALUES (pUuid, pFieldId, pValue)
    ON DUPLICATE KEY UPDATE Value = pValue;
END