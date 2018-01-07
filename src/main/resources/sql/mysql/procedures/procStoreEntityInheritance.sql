CREATE PROCEDURE procStoreEntityInheritance(IN pUuid VARCHAR(96), IN pEntityId BIGINT)
BEGIN
	INSERT IGNORE INTO JdsEntityInstance(Uuid, EntityId)
    VALUES (pUuid, pEntityId);
END