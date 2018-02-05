CREATE TABLE jds_entity_overview
(
  composite_key         VARCHAR(195),
  uuid                  VARCHAR(96),
  uuid_location         VARCHAR(56),
  uuid_location_version INTEGER,
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BOOLEAN,
  last_edit             DATETIME,
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE,
  PRIMARY KEY (composite_key)
);