CREATE TABLE jds_store_date_time (
  field_id      BIGINT,
  composite_key VARCHAR(195),
  value         DATETIME,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);