CREATE TABLE jds_store_integer (
  composite_key NVARCHAR2(128),
  field_id      NUMBER(19),
  sequence      NUMBER(10),
  value         NUMBER(10),
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
)