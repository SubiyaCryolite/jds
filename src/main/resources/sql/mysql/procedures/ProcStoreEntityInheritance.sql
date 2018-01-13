CREATE PROCEDURE proc_store_entity_inheritance(IN PUUID VARCHAR(96), IN PENTITY_ID BIGINT)
  BEGIN
    INSERT IGNORE INTO jds_entity_instance (uuid, entity_id)
    VALUES (puuid, pentity_id);
  END