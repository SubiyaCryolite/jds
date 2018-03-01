CREATE FUNCTION proc_store_zoned_date_time(p_composite_key VARCHAR(128),
                                           p_field_id      BIGINT,
                                           p_sequence      INTEGER,
                                           p_value         TIMESTAMP WITH TIME ZONE)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_zoned_date_time (composite_key, field_id, sequence, value)
  VALUES (p_composite_key, p_field_id, p_sequence, p_value)
  ON CONFLICT (composite_key, field_id, sequence)
    DO UPDATE SET value = p_value;
END;
$$
LANGUAGE plpgsql;