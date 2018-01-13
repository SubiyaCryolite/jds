CREATE TABLE jds_store_boolean (
  field_id BIGINT,
  uuid     VARCHAR(96),
  value    BOOLEAN,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);