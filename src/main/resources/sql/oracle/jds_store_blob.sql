CREATE TABLE jds_store_blob (
  composite_key NVARCHAR2(128) NOT NULL,
  field_id      NUMBER(19)     NOT NULL,
  sequence      NUMBER(10)     NOT NULL,
  value         BLOB,
  CONSTRAINT jds_store_blob_uc UNIQUE (composite_key, field_id, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
)