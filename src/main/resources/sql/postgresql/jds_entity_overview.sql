CREATE TABLE jds_entity_overview
(
  composite_key         VARCHAR(128),
  uuid                  VARCHAR(64),
  uuid_location         VARCHAR(45),
  uuid_location_version INTEGER,
  parent_uuid           VARCHAR(64),
  parent_composite_key  VARCHAR(128),
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BOOLEAN,
  last_edit             TIMESTAMP,
  field_id              BIGINT,
  PRIMARY KEY (composite_key),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id) ON DELETE CASCADE,
  FOREIGN KEY (parent_composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
);