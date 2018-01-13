CREATE FUNCTION proc_ref_enum(p_field_id BIGINT, p_seq INTEGER, p_caption TEXT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_enum (field_id, seq, caption)
  VALUES (p_field_id, p_seq, p_caption)
  ON CONFLICT (field_id, seq)
    DO UPDATE SET caption = p_caption;
END;
$$ LANGUAGE plpgsql;