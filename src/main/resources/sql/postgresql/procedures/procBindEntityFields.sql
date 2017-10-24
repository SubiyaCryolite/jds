CREATE FUNCTION procBindEntityFields(pEntityId BIGINT, pFieldId BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsEntityFields(EntityId, FieldId)
    VALUES (pEntityId, pFieldId)
    ON CONFLICT (EntityId,FieldId) DO NOTHING;
END;
$$ LANGUAGE plpgsql;