CREATE TABLE jds_store_text (
  composite_key NVARCHAR2(128),
  field_id      NUMBER(19),
  sequence      NUMBER(10),
  value         NCLOB,
  PRIMARY KEY (field_id, composite_key),
  FOREIGN KEY (composite_key) REFERENCES jds_entity_overview_light (composite_key) ON DELETE CASCADE
)