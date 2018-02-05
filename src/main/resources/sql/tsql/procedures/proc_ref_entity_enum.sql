CREATE PROCEDURE proc_ref_entity_enum(@entity_id BIGINT, @field_id BIGINT)
AS
  BEGIN
    MERGE jds_ref_entity_enum AS dest
    USING (VALUES (@entity_id, @field_id)) AS src(entity_id, field_id)
    ON (src.entity_id = dest.entity_id AND src.field_id = dest.field_id)
    WHEN NOT MATCHED THEN
      INSERT (entity_id, field_id) VALUES (src.entity_id, src.field_id);
  END