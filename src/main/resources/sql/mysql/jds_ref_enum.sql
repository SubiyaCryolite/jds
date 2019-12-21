CREATE TABLE jds_ref_enum (
  field_id INTEGER,
  seq      INTEGER,
  name     VARCHAR(128),
  caption  VARCHAR(128),
  PRIMARY KEY (field_id, seq),
  FOREIGN KEY (field_id) REFERENCES jds_ref_field (id)
    ON DELETE CASCADE
);