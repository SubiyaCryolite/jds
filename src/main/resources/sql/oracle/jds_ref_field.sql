CREATE TABLE jds_ref_field (
  id           NUMBER(10),
  caption      NVARCHAR2(64),
  description  NVARCHAR2(256),
  field_type_ordinal NUMBER(10),
  PRIMARY KEY (id),
  FOREIGN KEY (field_type_ordinal) REFERENCES jds_ref_field_type (ordinal) ON DELETE CASCADE
)