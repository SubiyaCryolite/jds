CREATE TABLE jds_entity_binding (
  parent_composite_key NVARCHAR2(128),
  child_composite_key  NVARCHAR2(128),
  field_id             NUMBER(19),
  child_entity_id      NUMBER(19),
  FOREIGN KEY (parent_composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE,
  FOREIGN KEY (child_composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
)