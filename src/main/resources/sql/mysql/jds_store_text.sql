CREATE TABLE jds_store_text (
  field_id      BIGINT,
  composite_key VARCHAR(195),
  value         TEXT,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);