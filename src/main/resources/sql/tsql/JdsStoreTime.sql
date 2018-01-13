CREATE TABLE jds_store_time (
  field_id BIGINT,
  uuid     NVARCHAR(96),
  value    TIME(7),
  PRIMARY KEY (field_id, uuid),
  CONSTRAINT fk_jds_store_time_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);