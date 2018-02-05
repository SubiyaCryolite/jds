CREATE PROCEDURE proc_ref_entity(@id BIGINT, @caption NVARCHAR(256))
AS
  BEGIN
    MERGE jds_ref_entity AS dest
    USING (VALUES (@id, @caption)) AS src(id, caption)
    ON (src.id = dest.id)
    WHEN NOT MATCHED THEN
      INSERT (id, caption) VALUES (src.id, src.caption)
    WHEN MATCHED THEN
      UPDATE SET dest.caption = src.caption;
  END