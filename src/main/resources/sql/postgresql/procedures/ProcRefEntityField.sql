CREATE FUNCTION proc_ref_entity_field(pentity_id BIGINT, pfield_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity_field (entity_id, field_id)
  VALUES (pentity_id, pfield_id)
  ON CONFLICT (entity_id, field_id)
    DO NOTHING;
END;
$$ LANGUAGE plpgsql;