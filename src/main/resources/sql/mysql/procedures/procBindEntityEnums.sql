CREATE PROCEDURE procBindEntityEnums(IN pEntityId BIGINT, IN pFieldId BIGINT)
BEGIN
	INSERT IGNORE INTO JdsEntityEnums(EntityId, FieldId)
    VALUES (pEntityId, pFieldId);
END;