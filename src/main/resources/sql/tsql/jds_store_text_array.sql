CREATE TABLE jds_store_text_array (
  composite_key NVARCHAR(128) NOT NULL,
  field_id      BIGINT,
  sequence      INTEGER,
  value         NVARCHAR(MAX),
  PRIMARY KEY (field_id, composite_key, sequence),
  CONSTRAINT fk_jds_store_text_array_parent_uuid FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);