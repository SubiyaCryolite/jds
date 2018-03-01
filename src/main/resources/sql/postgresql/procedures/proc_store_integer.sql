CREATE FUNCTION proc_store_integer(p_composite_key VARCHAR(128),
                                   p_field_id      BIGINT,
                                   p_sequence      INTEGER,
                                   p_value         INTEGER)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_integer (composite_key, field_id, sequence, value)
  VALUES (p_composite_key, p_field_id, p_sequence, p_value)
  ON CONFLICT (composite_key, field_id, sequence)
    DO UPDATE SET value = p_value;
END;
$$
LANGUAGE plpgsql;