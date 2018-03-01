CREATE TABLE jds_store_time (
  composite_key VARCHAR(128) NOT NULL,
  field_id      BIGINT NOT NULL,
  sequence      INTEGER NOT NULL,
  value         TIME WITHOUT TIME ZONE,
  CONSTRAINT jds_store_time_uc UNIQUE (composite_key, field_id, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
);