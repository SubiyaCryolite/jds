CREATE TABLE jds_store_long_array (
  field_id BIGINT,
  uuid     TEXT,
  sequence INTEGER,
  value    BIGINT,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);