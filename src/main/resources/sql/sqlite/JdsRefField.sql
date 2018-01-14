CREATE TABLE jds_ref_field (
  id           BIGINT,
  caption      TEXT,
  description  TEXT,
  type_ordinal INTEGER,
  PRIMARY KEY (id),
  FOREIGN KEY (type_ordinal) REFERENCES jds_ref_field_type (ordinal)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);