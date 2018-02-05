CREATE TABLE jds_ref_enum (
  field_id BIGINT,
  seq      INTEGER,
  caption  TEXT,
  PRIMARY KEY (field_id, seq),
  FOREIGN KEY (field_id) REFERENCES jds_ref_field (id) ON DELETE CASCADE
);