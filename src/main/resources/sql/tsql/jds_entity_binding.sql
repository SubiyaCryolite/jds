CREATE TABLE jds_entity_binding (
  parent_composite_key NVARCHAR(128),
  child_composite_key  NVARCHAR(128),
  field_id             BIGINT,
  child_entity_id      BIGINT,
  CONSTRAINT fk_jds_entity_binding_parent_uuid FOREIGN KEY (parent_composite_key) REFERENCES jds_entity_overview (composite_key),
  CONSTRAINT fk_jds_entity_binding_child_uuid FOREIGN KEY (child_composite_key) REFERENCES jds_entity_overview (composite_key)
);