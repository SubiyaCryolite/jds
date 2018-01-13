CREATE TABLE jds_store_date_time_array (
  field_id BIGINT,
  uuid     VARCHAR(96),
  sequence INT,
  value    DATETIME,
  PRIMARY KEY (field_id, uuid, sequence),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
);