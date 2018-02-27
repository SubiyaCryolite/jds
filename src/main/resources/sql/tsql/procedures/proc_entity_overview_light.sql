CREATE PROCEDURE proc_entity_overview_light(@composite_key NVARCHAR(128))
AS
  BEGIN
    MERGE jds_entity_overview AS dest
    USING (VALUES
      (@composite_key)) AS src(composite_key)
    ON (src.composite_key = dest.composite_key)
    WHEN NOT MATCHED THEN
      INSERT (composite_key)      VALUES (src.composite_key);
  END