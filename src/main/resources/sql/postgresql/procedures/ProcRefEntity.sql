CREATE FUNCTION proc_ref_entity(pid BIGINT, pcaption TEXT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity (id, caption)
  VALUES (pid, pcaption)
  ON CONFLICT (id)
    DO UPDATE SET caption = pcaption;
END;
$$ LANGUAGE plpgsql;