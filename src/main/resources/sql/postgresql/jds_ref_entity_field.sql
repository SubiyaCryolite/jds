CREATE TABLE jds_ref_entity_field (
  entity_id BIGINT,
  field_id  BIGINT,
  PRIMARY KEY (entity_id, field_id),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES jds_ref_field (id) ON DELETE CASCADE
);