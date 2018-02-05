CREATE FUNCTION proc_store_boolean(p_composite_key VARCHAR(128),
                                   p_field_id      BIGINT,
                                   p_value         BOOLEAN)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_boolean (composite_key, field_id, value)
  VALUES (p_composite_key, p_field_id, p_value)
  ON CONFLICT (composite_key, field_id)
    DO UPDATE SET value = p_value;
END;
$$
LANGUAGE plpgsql;