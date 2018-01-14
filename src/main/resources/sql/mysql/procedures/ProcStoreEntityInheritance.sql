CREATE PROCEDURE proc_store_entity_inheritance(IN p_entity_uuid VARCHAR(96), IN p_entity_id BIGINT)
  BEGIN
    INSERT IGNORE INTO jds_entity_instance (entity_uuid, entity_id)
    VALUES (p_entity_uuid, p_entity_id);
  END