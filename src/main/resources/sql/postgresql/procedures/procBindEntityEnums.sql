CREATE FUNCTION procBindEntityEnums(pEntityId BIGINT, pFieldId BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsBindEntityEnums(EntityId, FieldId)
    VALUES (pEntityId, pFieldId)
    ON CONFLICT (EntityId,FieldId) DO NOTHING;
END;
$$ LANGUAGE plpgsql;