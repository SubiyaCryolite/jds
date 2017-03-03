CREATE FUNCTION procStoreEntityOverview(pEntityGuid VARCHAR(48), pParentEntityGuid VARCHAR(48), pDateCreated TIMESTAMP, pDateModified TIMESTAMP, pEntityId BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreEntityOverview(EntityGuid, ParentEntityGuid, DateCreated, DateModified, EntityId)
    VALUES (pEntityGuid, pParentEntityGuid, pDateCreated, pDateModified, pEntityId)
    ON conflict (EntityGuid) do
    UPDATE SET ParentEntityGuid = pParentEntityGuid, DateCreated = pDateCreated, DateModified = pDateModified, EntityId = pEntityId;
END;
$$ LANGUAGE plpgsql;