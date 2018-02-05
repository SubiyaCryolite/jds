CREATE TABLE jds_store_blob (
  composite_key NVARCHAR(128) NOT NULL,
  field_id      BIGINT,
  value         VARBINARY(MAX),
  PRIMARY KEY (field_id, composite_key),
  CONSTRAINT fk_jds_store_blob_parent_uuid FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);