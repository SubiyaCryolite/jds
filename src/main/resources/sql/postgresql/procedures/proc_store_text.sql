CREATE FUNCTION proc_store_text(p_composite_key VARCHAR(195),
                                p_field_id      BIGINT,
                                p_value         TEXT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_text (composite_key, field_id, value)
  VALUES (p_composite_key, p_field_id, p_value)
  ON CONFLICT (composite_key, field_id)
    DO UPDATE SET value = p_value;
END;
$$
LANGUAGE plpgsql;