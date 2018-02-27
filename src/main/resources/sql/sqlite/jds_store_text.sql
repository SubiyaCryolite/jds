CREATE TABLE jds_store_text (
  composite_key TEXT,
  field_id      BIGINT,
  sequence      INTEGER,
  value         TEXT,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);