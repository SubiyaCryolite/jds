CREATE FUNCTION proc_store_time(puuid VARCHAR(96), pfield_id BIGINT, pvalue TIME WITHOUT TIME ZONE)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_time (uuid, field_id, value)
  VALUES (puuid, pfield_id, pvalue)
  ON CONFLICT (uuid, field_id)
    DO UPDATE SET value = pvalue;
END;
$$ LANGUAGE plpgsql;