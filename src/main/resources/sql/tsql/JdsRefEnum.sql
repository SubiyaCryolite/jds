CREATE TABLE jds_ref_enum (
  field_id BIGINT,
  seq      INTEGER,
  caption  NVARCHAR(MAX),
  PRIMARY KEY (field_id, seq)
);