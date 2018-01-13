CREATE TABLE jds_store_date_time (
  field_id BIGINT,
  uuid     NVARCHAR(96) NOT NULL,
  value    DATETIME,
  PRIMARY KEY (field_id, uuid),
  CONSTRAINT fk_jds_store_date_time_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);