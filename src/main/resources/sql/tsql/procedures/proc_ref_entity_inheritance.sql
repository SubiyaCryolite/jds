CREATE PROCEDURE proc_ref_entity_inheritance(@parent_entity_id BIGINT, @child_entity_id BIGINT)
AS
  BEGIN
    MERGE jds_ref_entity_inheritance AS dest
    USING (VALUES (@parent_entity_id, @child_entity_id)) AS src(parent_entity_id, child_entity_id)
    ON (src.parent_entity_id = dest.parent_entity_id AND src.child_entity_id = dest.child_entity_id)
    WHEN NOT MATCHED THEN
      INSERT (parent_entity_id, child_entity_id) VALUES (src.parent_entity_id, src.child_entity_id);
  END