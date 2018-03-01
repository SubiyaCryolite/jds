CREATE TABLE jds_store_time (
  composite_key NVARCHAR(128) UNIQUE NOT NULL,
  field_id      BIGINT NOT NULL,
  sequence      INTEGER NOT NULL,
  value         TIME(7),
  CONSTRAINT jds_store_time_uc UNIQUE (composite_key, field_id, sequence),
  CONSTRAINT jds_store_time_fk_composite_key FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);