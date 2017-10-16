CREATE PROCEDURE procStoreEntityInheritance(IN pEntityGuid VARCHAR(48), IN pEntityId BIGINT)
BEGIN
	INSERT IGNORE INTO JdsStoreEntityInheritance(EntityGuid, EntityId)
    VALUES (pEntityGuid, pEntityId);
END