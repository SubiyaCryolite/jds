CREATE TABLE jds_store_date_time (
  field_id BIGINT,
  uuid     TEXT,
  value    TIMESTAMP,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);