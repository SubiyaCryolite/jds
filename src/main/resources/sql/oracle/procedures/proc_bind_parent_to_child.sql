CREATE PROCEDURE proc_bind_parent_to_child(p_parent_entity_id IN NUMBER, p_child_entity_id IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_ref_entity_inheritance dest
    USING DUAL
    ON (p_parent_entity_id = parent_entity_id AND p_child_entity_id = child_entity_id)
    WHEN NOT MATCHED THEN
      INSERT (parent_entity_id, child_entity_id) VALUES (p_parent_entity_id, p_child_entity_id);
  END proc_bind_parent_to_child;