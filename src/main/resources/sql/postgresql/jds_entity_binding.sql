CREATE TABLE jds_entity_binding
(
  parent_uuid                  VARCHAR(64),
  parent_uuid_location         VARCHAR(45),
  parent_uuid_version INTEGER,
  child_uuid                   VARCHAR(64),
  child_uuid_location          VARCHAR(45),
  child_uuid_version  INTEGER,
  child_attribute_id           BIGINT,
  CONSTRAINT jds_entity_binding_uc UNIQUE (parent_uuid, parent_uuid_location, parent_uuid_version, child_uuid, child_uuid_location, child_uuid_version),
  FOREIGN KEY (parent_uuid, parent_uuid_location, parent_uuid_version)
  REFERENCES jds_entity_overview (uuid, uuid_location, uuid_version) ON DELETE CASCADE,
  FOREIGN KEY (child_uuid, child_uuid_location, child_uuid_version)
  REFERENCES jds_entity_overview (uuid, uuid_location, uuid_version) ON DELETE CASCADE
);