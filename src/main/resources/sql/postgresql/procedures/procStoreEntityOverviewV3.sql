CREATE FUNCTION procStoreEntityOverviewV3(pEntityGuid VARCHAR(48), pDateCreated TIMESTAMP, pDateModified TIMESTAMP, pLive BOOLEAN, pVersion BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreEntityOverview(EntityGuid, DateCreated, DateModified, Live, Version)
    VALUES (pEntityGuid, pDateCreated, pDateModified, pLive, pVersion)
    ON CONFLICT (EntityGuid) DO UPDATE SET DateModified = pDateModified, Live = pLive, Version = pVersion;
END;
$$ LANGUAGE plpgsql;