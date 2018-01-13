CREATE TABLE jds_store_integer (
  field_id BIGINT,
  uuid     NVARCHAR(96),
  value    INTEGER,
  PRIMARY KEY (field_id, uuid),
  CONSTRAINT fk_jds_store_integer_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);