CREATE PROCEDURE proc_store_entity_inheritance(IN p_entity_composite_key VARCHAR(128), IN p_entity_id BIGINT)
  BEGIN
    INSERT IGNORE INTO jds_entity_instance (entity_composite_key, entity_id)
    VALUES (p_entity_composite_key, p_entity_id);
  END