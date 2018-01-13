CREATE FUNCTION proc_store_entity_overview_v3(puuid VARCHAR(96), pdate_created TIMESTAMP, pdate_modified TIMESTAMP,
                                              plive BOOLEAN, pversion BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_entity_overview (uuid, date_created, date_modified, live, version)
  VALUES (puuid, pdate_created, pdate_modified, plive, pversion)
  ON CONFLICT (uuid)
    DO UPDATE SET date_modified = pdate_modified, live = plive, VERSION = pversion;
END;
$$ LANGUAGE plpgsql;