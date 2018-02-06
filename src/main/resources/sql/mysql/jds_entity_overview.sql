CREATE TABLE jds_entity_overview
(
  composite_key         VARCHAR(128),
  uuid                  VARCHAR(64),
  uuid_location         VARCHAR(45),
  uuid_location_version INTEGER,
  parent_uuid           VARCHAR(64),
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BOOLEAN,
  last_edit             DATETIME,
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE,
  PRIMARY KEY (composite_key)
);