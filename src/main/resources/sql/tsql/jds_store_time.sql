CREATE TABLE jds_store_time (
  composite_key NVARCHAR(128) NOT NULL,
  field_id      BIGINT,
  value         TIME(7),
  PRIMARY KEY (field_id, composite_key),
  CONSTRAINT jds_store_time_fk_composite_key FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);