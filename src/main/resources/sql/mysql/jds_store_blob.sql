CREATE TABLE jds_store_blob (
  composite_key VARCHAR(128),
  field_id      BIGINT,
  value         BLOB,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key)
    ON DELETE CASCADE
);