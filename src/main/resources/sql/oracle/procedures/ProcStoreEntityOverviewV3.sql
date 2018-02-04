CREATE PROCEDURE proc_store_entity_overview_v3(puuid IN NVARCHAR2, plive IN NUMBER, pversion IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_entity_overview dest
    USING DUAL
    ON (puuid = uuid)
    WHEN MATCHED THEN
      UPDATE SET live = plive, version = pversion
    WHEN NOT MATCHED THEN
      INSERT (uuid, live, version)
      VALUES (puuid, plive, pversion);
  END proc_store_entity_overview_v3;