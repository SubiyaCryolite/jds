CREATE TABLE jds_ref_field (
  id           BIGINT,
  caption      NVARCHAR(128),
  description  NVARCHAR(256),
  type_ordinal INTEGER,
  PRIMARY KEY (id),
  CONSTRAINT fk_jds_ref_field_type_ordinal FOREIGN KEY (type_ordinal) REFERENCES jds_ref_field_type (ordinal)
    ON DELETE NO ACTION
);