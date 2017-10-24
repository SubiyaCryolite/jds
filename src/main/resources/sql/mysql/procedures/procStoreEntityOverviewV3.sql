CREATE PROCEDURE procStoreEntityOverviewV3(IN pUuid VARCHAR(48), IN pDateCreated DATETIME, IN pDateModified DATETIME, IN pLive BOOLEAN, IN pVersion BIGINT)
BEGIN
	INSERT INTO JdsEntityOverview(Uuid, DateCreated, DateModified, Live, Version)
    VALUES (pUuid, pDateCreated, pDateModified, pLive, pVersion)
    ON DUPLICATE KEY UPDATE DateModified = pDateModified, Live = pLive, Version = pVersion;
END