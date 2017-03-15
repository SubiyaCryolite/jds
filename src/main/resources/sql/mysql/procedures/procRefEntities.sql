CREATE PROCEDURE procRefEntities(IN pEntityId BIGINT, IN pEntityName TEXT)
BEGIN
	INSERT INTO JdsRefEntities(EntityId, EntityName)
    VALUES (pEntityId, pEntityName)
    ON DUPLICATE KEY UPDATE EntityName = pEntityName;
END