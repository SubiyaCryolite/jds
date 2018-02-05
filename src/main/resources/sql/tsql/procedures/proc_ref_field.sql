CREATE PROCEDURE proc_ref_field(@id BIGINT, @caption NVARCHAR(128), @description NVARCHAR(256), @type_ordinal INTEGER)
AS
  BEGIN
    MERGE jds_ref_field AS dest
    USING (VALUES (@id, @caption, @description, @type_ordinal)) AS src(id, caption, description, type_ordinal)
    ON (src.id = dest.id)
    WHEN NOT MATCHED THEN
      INSERT (id, caption, description, type_ordinal) VALUES (src.id, src.caption, src.description, src.type_ordinal)
    WHEN MATCHED THEN
      UPDATE SET dest.caption = src.caption, dest.description = src.description, dest.type_ordinal = src.type_ordinal;
  END