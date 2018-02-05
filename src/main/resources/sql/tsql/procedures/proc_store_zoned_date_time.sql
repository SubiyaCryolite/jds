CREATE PROCEDURE proc_store_zoned_date_time(@composite_key NVARCHAR(128), @field_id BIGINT, @value DATETIMEOFFSET(7))
AS
  BEGIN
    MERGE jds_store_zoned_date_time AS dest
    USING (VALUES (@composite_key, @field_id, @value)) AS src(composite_key, field_id, value)
    ON (src.composite_key = dest.composite_key AND src.field_id = dest.field_id)
    WHEN MATCHED THEN
      UPDATE SET dest.value = src.value
    WHEN NOT MATCHED THEN
      INSERT (composite_key, field_id, value) VALUES (src.composite_key, src.field_id, src.value);
  END