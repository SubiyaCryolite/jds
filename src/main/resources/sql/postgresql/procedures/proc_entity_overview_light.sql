CREATE FUNCTION proc_entity_overview_light(p_composite_key VARCHAR(128))
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_entity_overview_light (composite_key)
  VALUES (p_composite_key)
  ON CONFLICT (composite_key)
    DO NOTHING;
END;
$$
LANGUAGE plpgsql;