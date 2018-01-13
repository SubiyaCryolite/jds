CREATE PROCEDURE proc_bind_parent_to_child(pparent_entity_id IN NUMBER, pchild_entity_id IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_ref_entity_inheritance dest
    USING DUAL
    ON (pparent_entity_id = parent_entity_id AND pchild_entity_id = child_entity_id)
    WHEN NOT MATCHED THEN
      INSERT (parent_entity_id, child_entity_id) VALUES (pparent_entity_id, pchild_entity_id);
  END proc_bind_parent_to_child;