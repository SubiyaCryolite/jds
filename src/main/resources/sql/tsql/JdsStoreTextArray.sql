CREATE TABLE jds_store_text_array (
  field_id BIGINT,
  uuid     NVARCHAR(96),
  sequence INTEGER,
  value    NVARCHAR(MAX),
  PRIMARY KEY (field_id, uuid, sequence),
  CONSTRAINT fk_jds_store_text_array_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);