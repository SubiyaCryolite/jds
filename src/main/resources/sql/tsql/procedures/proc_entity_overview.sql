CREATE PROCEDURE proc_store_entity_overview_v3(@composite_key         NVARCHAR(128),
                                               @uuid                  NVARCHAR(64),
                                               @uuid_location         NVARCHAR(45),
                                               @uuid_location_version INTEGER,
                                               @parent_uuid           NVARCHAR(64),
                                               @parent_composite_key  NVARCHAR(128),
                                               @entity_id             BIGINT,
                                               @entity_version        BIGINT,
                                               @live                  BIT,
                                               @last_edit             DATETIME)
AS
  BEGIN
    MERGE jds_entity_overview AS dest
    USING (VALUES
      (@composite_key, @uuid, @uuid_location, @uuid_location_version, @parent_uuid, @parent_composite_key, @entity_id,
       @entity_version, @live,
       @last_edit)) AS src(composite_key, uuid, uuid_location, uuid_location_version, parent_uuid, parent_composite_key, entity_id,
          entity_version, live, last_edit)
    ON (src.composite_key = dest.composite_key)
    WHEN MATCHED THEN
      UPDATE SET dest.uuid         = src.uuid,
        dest.uuid_location         = src.uuid_location,
        dest.uuid_location_version = src.uuid_location_version,
        dest.parent_uuid           = src.parent_uuid,
        dest.parent_composite_key  = src.parent_composite_key,
        dest.entity_id             = src.entity_id,
        dest.entity_version        = src.entity_version,
        dest.live                  = src.live,
        dest.last_edit             = src.last_edit
    WHEN NOT MATCHED THEN
      INSERT (
        composite_key,
        uuid,
        uuid_location,
        uuid_location_version,
        parent_uuid,
        parent_composite_key,
        entity_id,
        entity_version,
        live,
        last_edit)
      VALUES (
        src.composite_key,
        src.uuid,
        src.uuid_location,
        src.uuid_location_version,
        src.parent_uuid,
        src.parent_composite_key,
        src.entity_id,
        src.entity_version,
        src.live,
        src.last_edit);
  END