CREATE TABLE jds_ref_enum (
  field_id BIGINT,
  seq      INTEGER,
  name     NVARCHAR(128),
  caption  NVARCHAR(128),
  PRIMARY KEY (field_id, seq),
  CONSTRAINT fk_jds_ref_enum_field_id FOREIGN KEY (field_id) REFERENCES jds_ref_field (id)
    ON DELETE CASCADE
);