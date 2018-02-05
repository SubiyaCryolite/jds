CREATE TABLE jds_entity_instance (
  entity_uuid NVARCHAR2(64),
  entity_id   NUMBER(19),
  CONSTRAINT unique_entity_inheritance UNIQUE (entity_uuid, entity_id),
  FOREIGN KEY (entity_uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE,
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id) ON DELETE CASCADE
)