CREATE TABLE jds_store_double_array (
  field_id      BIGINT,
  composite_key VARCHAR(195),
  sequence      INT,
  value         DOUBLE,
  PRIMARY KEY (field_id, composite_key, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);