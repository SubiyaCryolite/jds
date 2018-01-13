CREATE TABLE jds_store_boolean (
  field_id BIGINT,
  uuid     NVARCHAR(96),
  value    BIT,
  PRIMARY KEY (field_id, uuid),
  CONSTRAINT fk_jds_store_boolean_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);