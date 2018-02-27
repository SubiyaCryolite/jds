CREATE TABLE jds_store_text (
  composite_key VARCHAR(128),
  field_id      BIGINT,
  sequence      INTEGER,
  value         TEXT,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key) ON DELETE CASCADE
);