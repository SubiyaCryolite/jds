CREATE PROCEDURE proc_store_entity_inheritance(@entity_composite_key NVARCHAR(128), @entity_id BIGINT)
AS
  BEGIN
    MERGE jds_entity_instance AS dest
    USING (VALUES (@entity_composite_key, @entity_id)) AS src(entity_composite_key, entity_id)
    ON (src.entity_composite_key = dest.entity_composite_key AND src.entity_id = dest.entity_id)
    WHEN NOT MATCHED THEN
      INSERT (entity_composite_key, [entity_id]) VALUES (src.entity_composite_key, src.entity_id);
  END