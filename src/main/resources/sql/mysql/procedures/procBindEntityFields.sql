CREATE PROCEDURE procBindEntityFields(IN pEntityId BIGINT, IN pFieldId BIGINT)
BEGIN
	INSERT IGNORE INTO JdsEntityFields(EntityId, FieldId)
    VALUES (pEntityId, pFieldId);
END