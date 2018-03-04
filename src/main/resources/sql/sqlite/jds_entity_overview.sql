CREATE TABLE jds_entity_overview
(
  composite_key         TEXT,
  uuid                  TEXT,
  uuid_location         TEXT,
  uuid_version INTEGER,
  parent_uuid           TEXT,
  parent_composite_key  TEXT,
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BOOLEAN,
  last_edit             TIMESTAMP,
  field_id              BIGINT,
  PRIMARY KEY (composite_key),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED
);