CREATE TABLE jds_ref_field (
  id           INTEGER PRIMARY KEY,
  caption      TEXT,
  description  TEXT,
  field_type_ordinal INTEGER,
  FOREIGN KEY (field_type_ordinal) REFERENCES jds_ref_field_type (ordinal)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);