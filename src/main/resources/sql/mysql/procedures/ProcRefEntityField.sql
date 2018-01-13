CREATE PROCEDURE proc_ref_entity_field(IN PENTITY_ID BIGINT, IN PFIELD_ID BIGINT)
  BEGIN
    INSERT IGNORE INTO jds_ref_entity_field (entity_id, field_id)
    VALUES (pentity_id, pfield_id);
  END