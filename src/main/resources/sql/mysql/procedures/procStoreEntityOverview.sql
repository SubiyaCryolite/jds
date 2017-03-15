CREATE PROCEDURE procStoreEntityOverview(IN pEntityGuid VARCHAR(48), IN pDateCreated DATETIME, IN pDateModified DATETIME, IN pEntityId BIGINT)
BEGIN
	INSERT INTO JdsStoreEntityOverview(EntityGuid, DateCreated, DateModified, EntityId)
    VALUES (pEntityGuid, pDateCreated, pDateModified, pEntityId)
    ON DUPLICATE KEY UPDATE DateCreated = pDateCreated, DateModified = pDateModified, EntityId = pEntityId;
END