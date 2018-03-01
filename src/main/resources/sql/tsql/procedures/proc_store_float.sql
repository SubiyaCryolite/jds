CREATE PROCEDURE proc_store_float(@composite_key NVARCHAR(128),
                                  @field_id      BIGINT,
                                  @sequence      INTEGER,
                                  @value         REAL)
AS
  BEGIN
    MERGE jds_store_float AS dest
    USING (VALUES (@composite_key, @field_id, @sequence, @value)) AS src(composite_key, field_id, sequence, value)
    ON (src.composite_key = dest.composite_key AND src.field_id = dest.field_id AND src.sequence = dest.sequence)
    WHEN MATCHED THEN
      UPDATE SET dest.value = src.value
    WHEN NOT MATCHED THEN
      INSERT (composite_key, field_id, sequence, value)
      VALUES (src.composite_key, src.field_id, src.sequence, src.value);
  END