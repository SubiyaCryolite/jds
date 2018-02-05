CREATE TABLE jds_entity_binding (
  parent_composite_key TEXT,
  child_composite_key  TEXT,
  field_id             BIGINT,
  child_entity_id      BIGINT,
  FOREIGN KEY (parent_composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED, --we use REPLACE INTO, so hopefully this maintains integrity,
  FOREIGN KEY (child_composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);