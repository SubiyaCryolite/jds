CREATE TABLE jds_store_double_array (
  composite_key TEXT,
  field_id      BIGINT,
  sequence      INTEGER,
  value         DOUBLE,
  PRIMARY KEY (field_id, composite_key, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);