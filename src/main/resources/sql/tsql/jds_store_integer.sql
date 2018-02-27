CREATE TABLE jds_store_integer (
  composite_key NVARCHAR(128) NOT NULL,
  field_id      BIGINT,
  sequence      INTEGER,
  value         INTEGER,
  PRIMARY KEY (field_id, composite_key),
  CONSTRAINT fk_jds_store_integer_parent_uuid FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key)
    ON DELETE CASCADE
);