CREATE FUNCTION proc_store_entity_inheritance(puuid VARCHAR(96), pentity_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_entity_instance (uuid, entity_id)
  VALUES (puuid, pentity_id)
  ON CONFLICT ON CONSTRAINT unique_entity_instance
    DO NOTHING;
END;
$$ LANGUAGE plpgsql;