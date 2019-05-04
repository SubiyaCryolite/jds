CREATE FUNCTION proc_ref_field(p_id BIGINT, p_caption VARCHAR(64), p_description VARCHAR(256), p_type_ordinal INTEGER)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_field (id, caption, description, type_ordinal)
  VALUES (p_id, p_caption, p_description, p_type_ordinal)
  ON CONFLICT (id)
    DO UPDATE SET caption = p_caption, description = p_description, type_ordinal = p_type_ordinal;
END;
$$
LANGUAGE plpgsql;