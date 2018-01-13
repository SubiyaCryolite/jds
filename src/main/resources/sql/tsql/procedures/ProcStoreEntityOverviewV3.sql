CREATE PROCEDURE proc_store_entity_overview_v3(@uuid NVARCHAR(96), @date_created DATETIME, @date_modified DATETIME,
                                               @live BIT, @version BIGINT)
AS
  BEGIN
    MERGE jds_entity_overview AS dest
    USING (VALUES (@uuid, @date_created, @date_modified, @live,
                   @version)) AS src(uuid, date_created, date_modified, live, VERSION)
    ON (src.uuid = dest.uuid)
    WHEN MATCHED THEN
      UPDATE SET dest.date_modified = src.date_modified, dest.live = src.live, dest.VERSION = src.VERSION
    WHEN NOT MATCHED THEN
      INSERT (uuid, date_created, date_modified, live, VERSION)
      VALUES (src.uuid, src.date_created, src.date_modified, src.live, src.version);
  END