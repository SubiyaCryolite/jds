CREATE PROCEDURE proc_store_entity_overview_v3(IN p_composite_key         VARCHAR(195),
                                               IN p_uuid                  VARCHAR(195),
                                               IN p_uuid_location         VARCHAR(56),
                                               IN p_uuid_location_version INTEGER,
                                               IN p_entity_id             BIGINT,
                                               IN p_live                  BOOLEAN,
                                               IN p_entity_version        BIGINT,
                                               IN p_last_edit             DATETIME)
  BEGIN
    INSERT INTO jds_entity_overview (composite_key, uuid, uuid_location, uuid_location_version, live, entity_version, entity_id, last_edit)
    VALUES
      (p_composite_key, p_uuid, p_uuid_location, p_uuid_location_version, p_live, p_entity_version, p_entity_id,
       p_last_edit)
    ON DUPLICATE KEY UPDATE uuid = p_uuid,
      uuid_location              = p_uuid_location,
      uuid_location_version      = p_uuid_location_version,
      entity_id                  = p_entity_id,
      live                       = p_live,
      entity_version             = p_entity_version,
      last_edit                  = p_last_edit;
  END