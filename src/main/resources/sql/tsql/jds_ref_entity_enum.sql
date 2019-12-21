CREATE TABLE jds_ref_entity_enum (
  entity_id INTEGER,
  field_id  INTEGER,
  PRIMARY KEY (entity_id, field_id),
  CONSTRAINT fk_jds_ref_entity_enum_entity_id FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_jds_ref_entity_enum_field_id FOREIGN KEY (field_id) REFERENCES jds_ref_field (id)
    ON DELETE CASCADE
);