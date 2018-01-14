CREATE PROCEDURE proc_store_entity_inheritance(@entity_uuid NVARCHAR(96), @entity_id BIGINT)
AS
  BEGIN
    MERGE jds_entity_instance AS dest
    USING (VALUES (@entity_uuid, @entity_id)) AS src(entity_uuid, entity_id)
    ON (src.entity_uuid = dest.entity_uuid AND src.entity_id = dest.entity_id)
    WHEN NOT MATCHED THEN
      INSERT (entity_uuid, [entity_id]) VALUES (src.entity_uuid, src.entity_id);
  END