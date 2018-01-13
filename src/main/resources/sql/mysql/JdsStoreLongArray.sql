CREATE TABLE jds_store_long_array (
  field_id BIGINT,
  uuid     VARCHAR(96),
  sequence INT,
  value    BIGINT,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);