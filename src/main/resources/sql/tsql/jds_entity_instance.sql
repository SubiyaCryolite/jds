CREATE TABLE jds_entity_instance (
  entity_composite_key NVARCHAR(128),
  entity_id            BIGINT,
  CONSTRAINT unique_entity_inheritance UNIQUE (entity_composite_key, entity_id),
  CONSTRAINT fk_unique_entity_inheritance_entity_uuid FOREIGN KEY (entity_composite_key) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE,
  CONSTRAINT fk_unique_entity_inheritance_entity_id FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);