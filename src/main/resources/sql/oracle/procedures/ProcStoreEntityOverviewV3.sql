CREATE PROCEDURE proc_store_entity_overview_v3(puuid          IN NVARCHAR2, pdate_created IN TIMESTAMP,
                                               pdate_modified IN TIMESTAMP, plive IN NUMBER, pversion IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_entity_overview dest
    USING DUAL
    ON (puuid = uuid)
    WHEN MATCHED THEN
      UPDATE SET date_modified = pdate_modified, live = plive, version = pversion
    WHEN NOT MATCHED THEN
      INSERT (uuid, date_created, date_modified, live, version)
      VALUES (puuid, pdate_created, pdate_modified, plive, pversion);
  END proc_store_entity_overview_v3;