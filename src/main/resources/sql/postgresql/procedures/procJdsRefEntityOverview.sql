CREATE FUNCTION procJdsRefEntityOverview(pEntityGuid VARCHAR(48), pEntityId BIGINT, pDateCreated TIMESTAMP, pDateModified TIMESTAMP)
RETURNS INTEGER AS $$
BEGIN
	INSERT INTO JdsRefEntityOverview(EntityGuid, EntityId, DateCreated, DateModified)
    VALUES (pEntityGuid, pEntityId, pDateCreated,pDateModified)
    ON conflict (EntityId, EntityGuid) do
    UPDATE SET DateCreated = pDateCreated, DateModified = pDateModified;
    RETURN 1;
END;
$$ LANGUAGE plpgsql;