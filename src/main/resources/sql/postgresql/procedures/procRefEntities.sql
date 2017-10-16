CREATE FUNCTION procRefEntities(pEntityId BIGINT, pEntityName TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsRefEntities(EntityId, EntityName)
    VALUES (pEntityId, pEntityName)
    ON CONFLICT (EntityId) DO UPDATE SET EntityName = pEntityName;
END;
$$ LANGUAGE plpgsql;