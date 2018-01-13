CREATE PROCEDURE proc_store_entity_inheritance(puuid IN NVARCHAR2, pentity_id IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_entity_instance dest
    USING DUAL
    ON (puuid = uuid AND pentity_id = entity_id)
    WHEN NOT MATCHED THEN
      INSERT (uuid, entity_id) VALUES (puuid, pentity_id);
  END proc_store_entity_inheritance;