CREATE TABLE jds_entity_instance (
  entity_composite_key NVARCHAR2(128),
  entity_id            NUMBER(19),
  CONSTRAINT unique_entity_inheritance UNIQUE (entity_composite_key, entity_id),
  FOREIGN KEY (entity_composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE,
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id) ON DELETE CASCADE
)