CREATE FUNCTION procStoreEntityInheritance(pUuid VARCHAR(96), pEntityId BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsEntityInstance(Uuid, EntityId)
    VALUES (pUuid, pEntityId)
    ON CONFLICT ON CONSTRAINT unique_entity_instance DO NOTHING;
END;
$$ LANGUAGE plpgsql;