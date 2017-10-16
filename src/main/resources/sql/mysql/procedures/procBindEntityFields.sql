CREATE PROCEDURE procBindEntityFields(IN pEntityId BIGINT, IN pFieldId BIGINT)
BEGIN
	INSERT IGNORE INTO JdsBindEntityFields(EntityId, FieldId)
    VALUES (pEntityId, pFieldId);
END