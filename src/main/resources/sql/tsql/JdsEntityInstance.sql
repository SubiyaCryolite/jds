CREATE TABLE jds_entity_instance (
  entity_uuid NVARCHAR(96),
  entity_id   BIGINT,
  CONSTRAINT unique_entity_inheritance UNIQUE (entity_uuid, entity_id),
  CONSTRAINT fk_unique_entity_inheritance_entity_uuid FOREIGN KEY (entity_uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE,
  CONSTRAINT fk_unique_entity_inheritance_entity_id FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);