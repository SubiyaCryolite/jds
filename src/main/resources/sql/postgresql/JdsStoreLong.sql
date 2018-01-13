CREATE TABLE jds_store_long (
  field_id BIGINT,
  uuid     VARCHAR(96),
  value    BIGINT,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
);