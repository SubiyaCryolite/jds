CREATE PROCEDURE proc_ref_enum(@field_id BIGINT, @seq INTEGER, @caption NVARCHAR(64))
AS
  BEGIN
    MERGE jds_ref_enum AS dest
    USING (VALUES (@field_id, @seq, @caption)) AS src(field_id, seq, caption)
    ON (src.field_id = dest.field_id AND src.seq = dest.seq)
    WHEN NOT MATCHED THEN
      INSERT (field_id, seq, caption) VALUES (src.field_id, src.seq, src.caption)
    WHEN MATCHED THEN
      UPDATE SET dest.caption = src.caption;
  END