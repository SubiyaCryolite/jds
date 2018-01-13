CREATE TABLE jds_store_date_time_array (
  field_id BIGINT,
  uuid     TEXT,
  sequence INTEGER,
  value    TIMESTAMP,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);