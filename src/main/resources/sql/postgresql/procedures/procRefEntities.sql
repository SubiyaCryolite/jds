CREATE FUNCTION procRefEntities(pEntityId BIGINT, pEntityName TEXT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsEntities(EntityId, EntityName)
    VALUES (pEntityId, pEntityName)
    ON CONFLICT (EntityId) DO UPDATE SET EntityName = pEntityName;
END;
$$ LANGUAGE plpgsql;