CREATE PROCEDURE proc_store_entity_overview_v3(IN PUUID          VARCHAR(96), IN PDATE_CREATED DATETIME,
                                               IN PDATE_MODIFIED DATETIME, IN PLIVE BOOLEAN, IN PVERSION BIGINT)
  BEGIN
    INSERT INTO jds_entity_overview (uuid, date_created, date_modified, live, version)
    VALUES (puuid, pdate_created, pdate_modified, plive, pversion)
    ON DUPLICATE KEY UPDATE date_modified = pdate_modified, live = plive, VERSION = pversion;
  END