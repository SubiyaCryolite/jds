CREATE FUNCTION proc_store_integer(puuid VARCHAR(96), pfield_id BIGINT, pvalue INTEGER)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_integer (uuid, field_id, value)
  VALUES (puuid, pfield_id, pvalue)
  ON CONFLICT (uuid, field_id)
    DO UPDATE SET value = pvalue;
END;
$$ LANGUAGE plpgsql;