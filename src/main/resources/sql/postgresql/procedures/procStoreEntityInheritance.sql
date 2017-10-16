CREATE FUNCTION procStoreEntityInheritance(pEntityGuid VARCHAR(48), pEntityId BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreEntityInheritance(EntityGuid, EntityId)
    VALUES (pEntityGuid, pEntityId)
    ON CONFLICT ON CONSTRAINT unique_entity_inheritance DO NOTHING;
END;
$$ LANGUAGE plpgsql;