CREATE PROCEDURE proc_ref_entity_inheritance(IN p_parent_entity_id BIGINT,
                                             IN p_child_entity_id  BIGINT)
  BEGIN
    INSERT INTO jds_ref_entity_inheritance (parent_entity_id, child_entity_id)
    VALUES (p_parent_entity_id, p_child_entity_id)
    ON DUPLICATE KEY UPDATE parent_entity_id = p_parent_entity_id, child_entity_id = p_child_entity_id;
  END