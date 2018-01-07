CREATE FUNCTION procStoreEntityOverviewV3(pUuid VARCHAR(96), pDateCreated TIMESTAMP, pDateModified TIMESTAMP, pLive BOOLEAN, pVersion BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsEntityOverview(Uuid, DateCreated, DateModified, Live, Version)
    VALUES (pUuid, pDateCreated, pDateModified, pLive, pVersion)
    ON CONFLICT (Uuid) DO UPDATE SET DateModified = pDateModified, Live = pLive, Version = pVersion;
END;
$$ LANGUAGE plpgsql;