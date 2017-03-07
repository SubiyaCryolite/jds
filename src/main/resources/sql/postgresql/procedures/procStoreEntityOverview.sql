CREATE FUNCTION procStoreEntityOverview(pEntityGuid VARCHAR(48), pDateCreated TIMESTAMP, pDateModified TIMESTAMP, pEntityId BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreEntityOverview(EntityGuid, DateCreated, DateModified, EntityId)
    VALUES (pEntityGuid, pDateCreated, pDateModified, pEntityId)
    ON CONFLICT (EntityGuid) DO UPDATE SET DateCreated = pDateCreated, DateModified = pDateModified, EntityId = pEntityId;
END;
$$ LANGUAGE plpgsql;