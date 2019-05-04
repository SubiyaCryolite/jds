CREATE FUNCTION proc_ref_entity(p_id          BIGINT,
                                p_name        VARCHAR(64),
                                p_caption     VARCHAR(64),
                                p_description VARCHAR(256))
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity (id, name, caption, description)
  VALUES (p_id, p_name, p_caption, p_description)
  ON CONFLICT (id)
    DO UPDATE SET caption = p_caption;
END;
$$
LANGUAGE plpgsql;