CREATE PROCEDURE proc_store_zoned_date_time(@uuid NVARCHAR(96), @field_id BIGINT, @value DATETIMEOFFSET(7))
AS
  BEGIN
    MERGE jds_store_zoned_date_time AS dest
    USING (VALUES (@uuid, @field_id, @value)) AS src(uuid, field_id, value)
    ON (src.uuid = dest.uuid AND src.field_id = dest.field_id)
    WHEN MATCHED THEN
      UPDATE SET dest.VALUE = src.VALUE
    WHEN NOT MATCHED THEN
      INSERT (uuid, field_id, value) VALUES (src.uuid, src.field_id, src.value);
  END