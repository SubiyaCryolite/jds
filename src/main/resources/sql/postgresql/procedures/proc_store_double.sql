CREATE FUNCTION proc_store_double(p_composite_key VARCHAR(128),
                                  p_field_id      BIGINT,
                                  p_sequence      INTEGER,
                                  p_value         FLOAT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_double (composite_key, field_id, sequence, value)
  VALUES (p_composite_key, p_field_id, p_sequence, p_value)
  ON CONFLICT (composite_key, field_id, sequence)
    DO UPDATE SET value = p_value;
END;
$$
LANGUAGE plpgsql;