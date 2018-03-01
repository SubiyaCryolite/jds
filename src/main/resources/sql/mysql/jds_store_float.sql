CREATE TABLE jds_store_float (
  composite_key VARCHAR(128) NOT NULL,
  field_id      BIGINT       NOT NULL,
  sequence      INT          NOT NULL,
  value         FLOAT,
  CONSTRAINT jds_store_float_uc UNIQUE (composite_key, field_id, sequence),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);