CREATE PROCEDURE procStoreEntityOverviewV3(IN pEntityGuid VARCHAR(48), IN pDateCreated DATETIME, IN pDateModified DATETIME, IN pLive BOOLEAN, IN pVersion BIGINT)
BEGIN
	INSERT INTO JdsStoreEntityOverview(EntityGuid, DateCreated, DateModified, Live, Version)
    VALUES (pEntityGuid, pDateCreated, pDateModified, pLive, pVersion)
    ON DUPLICATE KEY UPDATE DateModified = pDateModified, Live = pLive, Version = pVersion;
END