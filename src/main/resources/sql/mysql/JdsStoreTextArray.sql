CREATE TABLE jds_store_text_array (
  field_id BIGINT,
  uuid     VARCHAR(96),
  sequence INT,
  value    TEXT,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);