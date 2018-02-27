CREATE TABLE jds_store_date_time (
  composite_key VARCHAR(128),
  field_id      BIGINT,
  sequence      INT,
  value         DATETIME,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key)
    ON DELETE CASCADE
);