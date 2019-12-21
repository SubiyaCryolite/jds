CREATE TABLE jds_ref_field (
  id           INTEGER,
  caption      NVARCHAR(64),
  description  NVARCHAR(256),
  field_type_ordinal INTEGER,
  PRIMARY KEY (id),
  CONSTRAINT fk_jds_ref_field_type_ordinal FOREIGN KEY (field_type_ordinal) REFERENCES jds_ref_field_type (ordinal)
    ON DELETE NO ACTION
);