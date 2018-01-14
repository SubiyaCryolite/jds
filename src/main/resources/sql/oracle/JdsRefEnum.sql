CREATE TABLE jds_ref_enum (
  field_id NUMBER(19),
  seq      NUMBER(10),
  caption  NCLOB,
  PRIMARY KEY (field_id, seq),
  FOREIGN KEY (field_id) REFERENCES jds_ref_field (id) ON DELETE CASCADE
)