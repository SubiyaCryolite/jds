CREATE PROCEDURE proc_store_entity_overview_v3(IN p_composite_key         VARCHAR(195),
                                               IN p_uuid                  VARCHAR(195),
                                               IN p_uuid_location         VARCHAR(56),
                                               IN p_uuid_location_version INTEGER,
                                               IN p_entity_id             BIGINT,
                                               IN p_live                  BOOLEAN,
                                               IN p_version               BIGINT)
  BEGIN
    INSERT INTO jds_entity_overview (composite_key, uuid, uuid_location, uuid_location_version, live, version, entity_id)
    VALUES (p_composite_key, p_uuid, p_uuid_location, p_uuid_location_version, p_live, p_version, p_entity_id)
    ON DUPLICATE KEY UPDATE uuid = p_uuid,
      uuid_location              = p_uuid_location,
      uuid_location_version      = p_uuid_location_version,
      entity_id                  = p_entity_id,
      live                       = p_live,
      version                    = p_version;
  END