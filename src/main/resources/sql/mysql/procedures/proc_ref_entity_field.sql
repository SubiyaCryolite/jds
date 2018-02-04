CREATE PROCEDURE proc_ref_entity_field(IN p_entity_id BIGINT, IN p_field_id BIGINT)
  BEGIN
    INSERT IGNORE INTO jds_ref_entity_field (entity_id, field_id)
    VALUES (p_entity_id, p_field_id);
  END