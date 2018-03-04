CREATE TABLE jds_entity_overview
(
  uuid                  VARCHAR(64),
  uuid_location         VARCHAR(45),
  uuid_version INTEGER,
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BOOLEAN,
  last_edit             TIMESTAMP,
  PRIMARY KEY (uuid, uuid_location, uuid_version),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id) ON DELETE CASCADE
);