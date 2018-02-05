CREATE TABLE jds_store_date_time_array (
  composite_key VARCHAR(128),
  field_id      BIGINT,
  sequence      INT,
  value         DATETIME,
  PRIMARY KEY (field_id, composite_key, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);