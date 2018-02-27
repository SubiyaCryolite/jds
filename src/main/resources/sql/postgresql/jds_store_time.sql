CREATE TABLE jds_store_time (
  composite_key VARCHAR(128),
  field_id      BIGINT,
  value         TIME WITHOUT TIME ZONE,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key) ON DELETE CASCADE
);