CREATE TABLE jds_store_zoned_date_time (
  composite_key VARCHAR(195),
  field_id      BIGINT,
  value         TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
);