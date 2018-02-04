CREATE FUNCTION proc_store_entity_overview_v3(puuid VARCHAR(96), plive BOOLEAN, pversion BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_entity_overview (uuid, live, version)
  VALUES (puuid, plive, pversion)
  ON CONFLICT (uuid)
    DO UPDATE SET live = plive, VERSION = pversion;
END;
$$
LANGUAGE plpgsql;