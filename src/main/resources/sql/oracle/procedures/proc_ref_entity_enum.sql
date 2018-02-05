CREATE PROCEDURE proc_ref_entity_enum(pentity_id IN NUMBER, p_field_id IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_ref_entity_enum dest
    USING DUAL
    ON (pentity_id = entity_id AND p_field_id = field_id)
    WHEN NOT MATCHED THEN
      INSERT (entity_id, field_id) VALUES (pentity_id, p_field_id);
  END proc_ref_entity_enum;