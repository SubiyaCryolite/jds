CREATE FUNCTION proc_store_entity_inheritance(p_entity_uuid VARCHAR(96), p_entity_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_entity_instance (entity_uuid, entity_id)
  VALUES (p_entity_uuid, p_entity_id)
  ON CONFLICT ON CONSTRAINT unique_entity_instance
    DO NOTHING;
END;
$$ LANGUAGE plpgsql;