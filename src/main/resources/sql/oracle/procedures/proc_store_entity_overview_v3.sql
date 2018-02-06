CREATE PROCEDURE proc_store_entity_overview_v3(p_composite_key         IN NVARCHAR2,
                                               p_uuid                  IN NVARCHAR2,
                                               p_uuid_location         IN NVARCHAR2,
                                               p_uuid_location_version IN NUMBER,
                                               p_parent_uuid           IN VARCHAR2,
                                               p_entity_id             IN NUMBER,
                                               p_entity_version        IN NUMBER,
                                               p_live                  IN NUMBER,
                                               p_last_edit             IN TIMESTAMP)
AS
  BEGIN
    MERGE INTO jds_entity_overview dest
    USING DUAL
    ON (p_composite_key = composite_key)
    WHEN MATCHED THEN
      UPDATE SET uuid         = p_uuid,
        uuid_location         = p_uuid_location,
        uuid_location_version = p_uuid_location_version,
        parent_uuid           = p_parent_uuid,
        entity_id             = p_entity_id,
        entity_version        = p_entity_version,
        live                  = p_live,
        last_edit             = p_last_edit
    WHEN NOT MATCHED THEN
      INSERT (composite_key, uuid, uuid_location, uuid_location_version, parent_uuid, entity_id, entity_version, live, last_edit)
      VALUES (p_composite_key, p_uuid, p_uuid_location, p_uuid_location_version, p_parent_uuid, p_entity_id,
              p_entity_version, p_live, p_last_edit);
  END proc_store_entity_overview_v3;