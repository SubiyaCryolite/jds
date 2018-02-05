CREATE TABLE jds_store_double (
  composite_key NVARCHAR(128) NOT NULL,
  field_id      BIGINT,
  value         FLOAT,
  PRIMARY KEY (field_id, composite_key),
  CONSTRAINT fk_jds_store_double_parent_uuid FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);