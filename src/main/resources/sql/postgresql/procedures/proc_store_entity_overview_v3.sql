CREATE FUNCTION proc_store_entity_overview_v3(p_composite_key         VARCHAR(128),
                                              p_uuid                  VARCHAR(64),
                                              p_uuid_location         VARCHAR(45),
                                              p_uuid_location_version INTEGER,
                                              p_parent_uuid           VARCHAR(64),
                                              p_entity_id             BIGINT,
                                              p_live                  BOOLEAN,
                                              p_entity_version        BIGINT,
                                              p_last_edit             TIMESTAMP)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_entity_overview (composite_key, uuid, uuid_location, uuid_location_version, parent_uuid, entity_id, live, entity_version, last_edit)
  VALUES (p_composite_key, p_uuid, p_uuid_location, p_uuid_location_version, p_parent_uuid, p_entity_id, p_live,
          p_entity_version,
          p_last_edit)
  ON CONFLICT (composite_key)
    DO UPDATE SET uuid      = p_uuid,
      uuid_location         = p_uuid_location,
      uuid_location_version = p_uuid_location_version,
      parent_uuid           = p_parent_uuid,
      entity_id             = p_entity_id,
      live                  = p_live,
      entity_version        = p_entity_version,
      last_edit             = p_last_edit;
END;
$$
LANGUAGE plpgsql;