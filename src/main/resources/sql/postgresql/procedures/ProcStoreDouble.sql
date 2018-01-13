CREATE FUNCTION proc_store_double(puuid VARCHAR(96), pfield_id BIGINT, pvalue FLOAT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_store_double (uuid, field_id, value)
  VALUES (puuid, pfield_id, pvalue)
  ON CONFLICT (uuid, field_id)
    DO UPDATE SET value = pvalue;
END;
$$ LANGUAGE plpgsql;