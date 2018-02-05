CREATE FUNCTION proc_ref_entity_enum(p_entity_id BIGINT, p_field_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity_enum (entity_id, field_id)
  VALUES (p_entity_id, p_field_id)
  ON CONFLICT (entity_id, field_id)
    DO NOTHING;
END;
$$
LANGUAGE plpgsql;