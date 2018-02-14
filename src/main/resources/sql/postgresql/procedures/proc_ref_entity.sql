CREATE FUNCTION proc_ref_entity(p_id          BIGINT,
                                p_name        VARCHAR(256),
                                p_caption     VARCHAR(256),
                                p_description VARCHAR(256),
                                p_parent      BOOLEAN)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity (id, name, caption, description, parent)
  VALUES (p_id, p_name, p_caption, p_description, p_parent)
  ON CONFLICT (id)
    DO UPDATE SET caption = p_caption;
END;
$$
LANGUAGE plpgsql;