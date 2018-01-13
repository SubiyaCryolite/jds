CREATE PROCEDURE proc_ref_entity_enum(pentity_id IN NUMBER, pfield_id IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_ref_entity_enum dest
    USING DUAL
    ON (pentity_id = entity_id AND pfield_id = field_id)
    WHEN NOT MATCHED THEN
      INSERT (entity_id, field_id) VALUES (pentity_id, pfield_id);
  END proc_ref_entity_enum;