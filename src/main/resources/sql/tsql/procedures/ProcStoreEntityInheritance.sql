CREATE PROCEDURE proc_store_entity_inheritance(@uuid NVARCHAR(96), @entity_id BIGINT)
AS
  BEGIN
    MERGE jds_entity_instance AS dest
    USING (VALUES (@uuid, @entity_id)) AS src(uuid, entity_id)
    ON (src.uuid = dest.uuid AND src.entity_id = dest.entity_id)
    WHEN NOT MATCHED THEN
      INSERT (uuid, entity_id) VALUES (src.uuid, src.entity_id);
  END