CREATE TABLE jds_ref_entity_field (
  entity_id INTEGER,
  field_id  INTEGER,
  PRIMARY KEY (entity_id, field_id),
  CONSTRAINT fk_jds_ref_entity_field_entity_id FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id),
  CONSTRAINT fk_jds_ref_entity_field_field_id FOREIGN KEY (field_id) REFERENCES jds_ref_field (id)
);