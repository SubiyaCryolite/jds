CREATE TABLE jds_store_double (
  field_id BIGINT,
  uuid     NVARCHAR(96) NOT NULL,
  value    FLOAT,
  PRIMARY KEY (field_id, uuid),
  CONSTRAINT fk_jds_store_double_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);