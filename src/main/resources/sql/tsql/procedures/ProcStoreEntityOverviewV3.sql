CREATE PROCEDURE proc_store_entity_overview_v3(@uuid NVARCHAR(96), @live BIT, @version BIGINT)
AS
  BEGIN
    MERGE jds_entity_overview AS dest
    USING (VALUES (@uuid, @live, @version)) AS src(uuid, live, VERSION)
    ON (src.uuid = dest.uuid)
    WHEN MATCHED THEN
      UPDATE SET dest.live = src.live, dest.VERSION = src.VERSION
    WHEN NOT MATCHED THEN
      INSERT (uuid, live, VERSION)
      VALUES (src.uuid, src.live, src.version);
  END