CREATE TABLE jds_store_blob (
  field_id BIGINT,
  uuid     VARCHAR(96),
  value    BLOB,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);