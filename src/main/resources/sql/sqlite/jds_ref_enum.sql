CREATE TABLE jds_ref_enum (
  field_id INTEGER,
  seq      INTEGER,
  name     TEXT,
  caption  TEXT,
  PRIMARY KEY (field_id, seq),
  FOREIGN KEY (field_id) REFERENCES jds_ref_field (id)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);