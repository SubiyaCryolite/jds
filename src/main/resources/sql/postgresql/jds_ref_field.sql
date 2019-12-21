CREATE TABLE jds_ref_field (
  id           INTEGER PRIMARY KEY,
  caption      VARCHAR(64),
  description  VARCHAR(256),
  field_type_ordinal INTEGER,
  FOREIGN KEY (field_type_ordinal) REFERENCES jds_ref_field_type (ordinal) ON DELETE CASCADE
);