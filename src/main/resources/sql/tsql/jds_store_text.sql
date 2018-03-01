CREATE TABLE jds_store_text (
  composite_key NVARCHAR(128) NOT NULL,
  field_id      BIGINT NOT NULL,
  sequence      INTEGER NOT NULL,
  value         NVARCHAR(MAX),
  CONSTRAINT jds_store_text_uc UNIQUE (composite_key, field_id, sequence),
  CONSTRAINT jds_store_text_fk_composite_key FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
);