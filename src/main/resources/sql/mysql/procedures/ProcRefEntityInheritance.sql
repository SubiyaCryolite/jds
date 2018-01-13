CREATE PROCEDURE proc_bind_parent_to_child(IN PPARENT_ENTITY_ID BIGINT, IN PCHILD_ENTITY_ID BIGINT)
  BEGIN
    INSERT INTO jds_ref_entity_inheritance (parent_entity_id, child_entity_id)
    VALUES (pparent_entity_id, pchild_entity_id)
    ON DUPLICATE KEY UPDATE parent_entity_id = pparent_entity_id, child_entity_id = pchild_entity_id;
  END